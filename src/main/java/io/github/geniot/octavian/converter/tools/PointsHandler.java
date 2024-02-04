package io.github.geniot.octavian.converter.tools;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.model.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PointsHandler {
    static Logger logger = LoggerFactory.getLogger(PointsHandler.class);

    public static IndexedTreeMap<Integer, Point> mergePointMaps(IndexedTreeMap<Integer, Point> toPointsMap,
                                                                IndexedTreeMap<Integer, Point> fromPointsMap) {
        for (Integer key : fromPointsMap.keySet()) {
            if (toPointsMap.containsKey(key)) {
                Point fromPoint = fromPointsMap.get(key);
                Point toPoint = toPointsMap.get(key);
                mergePoints(fromPoint, toPoint);
            } else {
                toPointsMap.put(key, fromPointsMap.get(key));
            }
        }
        return toPointsMap;
    }

    public static void mergePoints(Point fromPoint, Point toPoint) {
        if (toPoint.getOffsetX() == -1) {
            toPoint.setOffsetX(fromPoint.getOffsetX());
        }

        if (fromPoint.getOffsetX() != toPoint.getOffsetX()) {
            toPoint.setOffsetX(Math.min(fromPoint.getOffsetX(), toPoint.getOffsetX()));
        }

        toPoint.setNotesOn(ArrayUtils.addAll(toPoint.getNotesOn(), fromPoint.getNotesOn()));
        toPoint.setNotesOff(ArrayUtils.addAll(toPoint.getNotesOff(), fromPoint.getNotesOff()));
    }


    public void setOffsetsToOffPoints(Point[] points, float width) {
        int duration = points[points.length - 1].getTimestamp() + 35;
        //setting x offsets for OFF notes based on surrounding offsets and milliseconds
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            if (point.getNotesOn() == null && point.getNotesOff() != null) {
                Point prevOnPoint = getPrevOnPoint(points, i);
                Point nextOnPoint = getNextOnPoint(points, i);

                int prevOffset = prevOnPoint == null ? 0 : prevOnPoint.getOffsetX();
                int nextOffset = nextOnPoint == null ? (int) width : nextOnPoint.getOffsetX();
                if (prevOffset == -1 || nextOffset == -1) {
                    throw new RuntimeException("Surrounding offsets not initialized for " + i);
                }

                int thisTimestamp = point.getTimestamp();
                int prevTimestamp = prevOnPoint == null ? 0 : prevOnPoint.getTimestamp();
                int nextTimestamp = nextOnPoint == null ? duration : nextOnPoint.getTimestamp();

                int deltaOffset = nextOffset - prevOffset;
                int deltaTimestamp = nextTimestamp - prevTimestamp;
                int deltaThisTimestamp = thisTimestamp - prevTimestamp;
                float thisTimestampPercent = (float) deltaThisTimestamp * 100 / (float) deltaTimestamp;

                float deltaThisOffset = deltaOffset * (thisTimestampPercent / 100);
                if (deltaThisOffset < 1) {
                    deltaThisOffset = 1;
                }

                int thisOffset = prevOffset + (int) deltaThisOffset;

                point.setOffsetX(thisOffset);

            }
        }
    }

    public Point getPrevOnPoint(Point[] points, int i) {
        if (i == 0) {
            return null;
        }
        --i;
        while (i >= 0) {
            if (points[i].getNotesOn() != null) {
                return points[i];
            }
            --i;
        }
        return null;
    }

    public Point getNextOnPoint(Point[] points, int i) {
        if (i == points.length - 1) {
            return null;
        }
        ++i;
        while (i < points.length) {
            if (points[i].getNotesOn() != null) {
                return points[i];
            }
            ++i;
        }
        return null;
    }

    public void setBars(IndexedTreeMap<Integer, Point> pointsMap, int[] barOffsets) throws Exception {
        for (Point point : pointsMap.values()) {
            int insertionPoint = Arrays.binarySearch(barOffsets, point.getOffsetX());
            insertionPoint = Math.abs(insertionPoint) - 1;
            point.setBar(insertionPoint);
        }
    }

    public void validateOffsets(IndexedTreeMap<Integer, Point> pointsMap) throws Exception {
        for (Point point : pointsMap.values()) {
            if (point.getNotesOn() != null && point.getNotesOff() != null) {
                throw new Exception("Found on-off point.");
            }
        }
    }

    public void bassesToChords(IndexedTreeMap<Integer, Point> pointsMap) throws Exception {
        for (Point point : pointsMap.values()) {
            if (point.getNotesOn() != null && point.getNotesOn().length >= 3) {
                SortedSet<Integer> set = setFromNotes(point.getNotesOn());
                point.setNotesOn(new Note[0]);
                processBassesToChords(set, point, NoteState.ON);
            }
            if (point.getNotesOff() != null && point.getNotesOff().length >= 3) {
                SortedSet<Integer> set = setFromNotes(point.getNotesOff());
                point.setNotesOff(new Note[0]);
                processBassesToChords(set, point, NoteState.OFF);
            }
        }
    }


    private void processBassesToChords(SortedSet<Integer> set, Point point, NoteState state) throws Exception {
        if (set.size() == 3) {
            String chord = getChordName(set, point.getBar());

            Note note = new Note();
            note.setNoteType(NoteType.LEFT_HAND_CHORD);
            note.setChordName(chord);
            note.setNoteName(Constants.NOTE_NAME_MAP.get(String.valueOf(note.getChordName())));
            addNoteToPoint(note, point, state);
        } else if (set.size() == 4) {//can be a chord, or a chord and a bass
            //trying to identify the chord from 3 highest notes
            String chord = getChordName(set.tailSet((Integer) set.toArray()[1]), point.getBar());

            Note note = new Note();
            note.setNoteType(NoteType.LEFT_HAND_CHORD);
            note.setChordName(chord);
            note.setNoteName(Constants.NOTE_NAME_MAP.get(String.valueOf(note.getChordName())));
            addNoteToPoint(note, point, state);

            //lowest note
            Note noteLow = new Note();
            noteLow.setNoteType(NoteType.LEFT_HAND_BASS);
            noteLow.setNoteValue(set.first());
            noteLow.setNoteName(Constants.NOTE_NAME_MAP.get(String.valueOf(noteLow.getNoteValue())));
            addNoteToPoint(noteLow, point, state);
        } else {
            throw new Exception("Left-hand playing more than 4 notes.");
        }
    }

    private void addNoteToPoint(Note note, Point point, NoteState state) {
        Note[] notes = state.equals(NoteState.ON) ? point.getNotesOn() : point.getNotesOff();
        if (notes == null) {
            notes = new Note[]{};
        }
        List<Note> tmpL = new ArrayList<>(Arrays.asList(notes));
        tmpL.add(note);

        notes = tmpL.toArray(new Note[0]);
        if (state.equals(NoteState.ON)) {
            point.setNotesOn(notes);
        } else {
            point.setNotesOff(notes);
        }
    }

    public String getChordName(SortedSet<Integer> notesSet, int bar) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer i : notesSet) {
            stringBuilder.append(i);
            if (!i.equals(notesSet.last())) {
                stringBuilder.append("_");
            }
        }
        String chordName = Chord.CHORDS_MAP.get(stringBuilder.toString());
        if (chordName == null) {
            StringBuilder noteNames = new StringBuilder();
            String[] splits = stringBuilder.toString().split("_");
            for (String split : splits) {
                noteNames.append(Constants.NOTE_NAME_MAP.get(split));
                noteNames.append(" ");
            }
            throw new Exception("Unidentified chord. Couldn't find chord for : " + noteNames + "(" + stringBuilder + ") in bar: " + bar);
        }
        return chordName;
    }

    private SortedSet<Integer> setFromNotes(Note[] notes) {
        SortedSet<Integer> notesSet = new TreeSet<>();
        for (Note note : notes) {
            notesSet.add(note.getNoteValue());
        }
        return notesSet;
    }

    public void splitOnOff(IndexedTreeMap<Integer, Point> pointsMap) throws Exception {
        //patching, making sure there are no OFF-notes in the same point with ON-notes
        //this is necessary for correct player functioning
        List<Point> newOffPoints = new ArrayList<>();
        for (int i = 0; i < pointsMap.size(); i++) {
            Point thisPoint = pointsMap.get(pointsMap.exactKey(i));
            Point splitPoint = splitPoint(thisPoint);
            if (splitPoint != null) {
                thisPoint.setNotesOff(null);
                newOffPoints.add(splitPoint);
            }
        }
        if (!newOffPoints.isEmpty()) {
            logger.info("Adding " + newOffPoints.size() + " off points after on/off split.");
            for (Point newOffPoint : newOffPoints) {
                Point point = pointsMap.get(newOffPoint.getTimestamp());
                if (point == null) {
                    point = newOffPoint;
                } else {
                    if (point.getNotesOn() == null) {
                        point.setNotesOff(ArrayUtils.addAll(point.getNotesOff(), newOffPoint.getNotesOff()));
                    } else {
                        throw new Exception("Point with the on-notes and timestamp already exists.");
                    }
                }
                pointsMap.put(newOffPoint.getTimestamp(), point);
            }
        }
    }

    public static Point splitPoint(Point thisPoint) throws Exception {
        if (thisPoint.getNotesOn() != null && thisPoint.getNotesOff() != null) {
            Point offPoint = new Point();
            offPoint.setNotesOff(thisPoint.getNotesOff());
            offPoint.setTimestamp(thisPoint.getTimestamp() - 1);
            offPoint.setOffsetX(thisPoint.getOffsetX() - 1);
            if (offPoint.getOffsetX() == -1) {
                throw new Exception("Offset is not initialized.");
            }
            return offPoint;
        } else {
            return null;
        }
    }

    private Point findOffPointForOnNote(Note onNote, Point[] points, int from) {
        for (int i = from; i < points.length; i++) {
            Point point = points[i];
            if (point.getNotesOff() != null) {
                for (Note offNote : point.getNotesOff()) {
                    if (
                            offNote.getNoteType().equals(onNote.getNoteType()) &&
                                    Objects.equals(offNote.getNoteValue(), onNote.getNoteValue()) &&
                                    StringUtils.equals(offNote.getChordName(), onNote.getChordName())
                    ) {
                        return point;
                    }
                }
            }
        }
        throw new RuntimeException("Couldn't find OFF note for ON note: " + onNote.getNoteType());
    }

    public void changeTicksToMilliseconds(IndexedTreeMap<Integer, Point> pointsMap, TreeMap<Long, Float> tempoMap) throws Exception {
        Point[] points = pointsMap.values().toArray(new Point[0]);
        long currentTick = 0;
        int currentMillisecond = 0;
        for (Point point : points) {
            long tick = point.getTimestamp();

            Map.Entry<Long, Float> entry = tempoMap.ceilingEntry(tick);
            entry = entry == null ? tempoMap.floorEntry(tick) : entry;
            float tempo = entry.getValue();

            currentMillisecond += Math.abs(tick - currentTick) * tempo;
            point.setTimestamp(currentMillisecond);

            currentTick = tick;
        }
        pointsMap.clear();
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            updateNotesDurations(point, point.getNotesOn(), points, i);

            if (pointsMap.containsKey(point.getTimestamp())) {
                Point toPoint = pointsMap.get(point.getTimestamp());

                mergePoints(point, toPoint);

                Point splitPoint = splitPoint(toPoint);
                if (splitPoint != null) {
                    toPoint.setNotesOff(null);
                    pointsMap.put(splitPoint.getTimestamp(), splitPoint);
                }
            } else {
                pointsMap.put(point.getTimestamp(), point);
            }
        }
    }

    private void updateNotesDurations(Point thisPoint, Note[] notes, Point[] points, int from) {
        if (notes != null) {
            for (Note note : notes) {
                Point offPoint = findOffPointForOnNote(note, points, from);
                note.setDuration(Math.abs(thisPoint.getTimestamp() - offPoint.getTimestamp()));
            }
        }
    }
}
