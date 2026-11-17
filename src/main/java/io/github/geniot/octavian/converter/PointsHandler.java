package io.github.geniot.octavian.converter;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.json.Note;
import io.github.geniot.octavian.converter.json.NoteType;
import io.github.geniot.octavian.converter.json.Point;
import io.github.geniot.octavian.converter.midi.MidiNote;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import io.github.geniot.octavian.converter.midi.MidiTrack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PointsHandler {

    public void addPoints(IndexedTreeMap<Integer, Point> points,
                          MidiRoot midiRoot,
                          Map<String, Integer> noteOffsets) throws Exception {
        for (int i = 0; i < midiRoot.getTracks().size(); i++) {
            MidiTrack track = midiRoot.getTracks().get(i);
            for (MidiNote midiNote : track.getNotes()) {
                int duration = (int) (midiNote.getDuration() * 1000);
                int onTimestamp = (int) (midiNote.getTime() * 1000);
                int offTimestamp = onTimestamp + duration;
                int midiValue = midiNote.getMidi();

                Point onPoint = points.getOrDefault(onTimestamp, new Point());
                setOffsetX(onPoint, noteOffsets.get(midiNote.getId()));
                onPoint.setTimestamp(onTimestamp);
                Note onNote = new Note();
                onNote.setId(midiNote.getId());
                onNote.setNoteType(i == 0 ? NoteType.RIGHT_HAND_NOTE : NoteType.LEFT_HAND_BASS);
                onNote.setDuration(duration);
                onNote.setNoteValue(midiValue);
                onNote.setNoteName(Constants.NOTE_NAME_MAP.get(midiValue));
                onPoint.getNotesOn().add(onNote);

                Point offPoint = points.getOrDefault(offTimestamp, new Point());
                offPoint.setTimestamp(offTimestamp);
                Note offNote = new Note();
                offNote.setId(midiNote.getId());
                offNote.setNoteType(i == 0 ? NoteType.RIGHT_HAND_NOTE : NoteType.LEFT_HAND_BASS);
                offNote.setNoteValue(midiValue);
                offNote.setNoteName(Constants.NOTE_NAME_MAP.get(midiValue));
                offPoint.getNotesOff().add(offNote);

                points.put(onTimestamp, onPoint);
                points.put(offTimestamp, offPoint);
            }
        }
    }

    private void setOffsetX(Point onPoint, Integer offset) throws Exception {
        if (onPoint.getOffsetX() != null && !onPoint.getOffsetX().equals(offset)) {
            throw new Exception("Offset X not equal: " + onPoint.getOffsetX());
        }
        onPoint.setOffsetX(offset);
    }


    /**
     * Setting x offsets for OFF notes based on surrounding offsets and milliseconds.
     *
     * @param points
     * @param width
     */
    public void setOffsetsToOffPoints(ArrayList<Point> points, float width) {
        int duration = points.getLast().getTimestamp() + 35; //todo: find a better way for total duration
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (point.getNotesOn().isEmpty() && !point.getNotesOff().isEmpty()) {
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

    private Point getPrevOnPoint(ArrayList<Point> points, int i) {
        if (i == 0) {
            return null;
        }
        --i;
        while (i >= 0) {
            if (!points.get(i).getNotesOn().isEmpty()) {
                return points.get(i);
            }
            --i;
        }
        return null;
    }

    private Point getNextOnPoint(ArrayList<Point> points, int i) {
        if (i == points.size() - 1) {
            return null;
        }
        ++i;
        while (i < points.size()) {
            if (!points.get(i).getNotesOn().isEmpty()) {
                return points.get(i);
            }
            ++i;
        }
        return null;
    }

    public void setBars(IndexedTreeMap<Integer, Point> points, int[] barOffsets) {
        for (Point point : points.values()) {
            int insertionPoint = Arrays.binarySearch(barOffsets, point.getOffsetX());
            insertionPoint = insertionPoint < 0 ? Math.abs(insertionPoint) - 1 : insertionPoint;
            point.setBar(insertionPoint);
        }
    }

    public void convertBassNotesToChords(IndexedTreeMap<Integer, Point> points) throws Exception {
        for (Point point : points.values()) {
            point.setNotesOn(notes2chords(point.getNotesOn(), true));
            point.setNotesOff(notes2chords(point.getNotesOff(), false));
        }
    }

    private List<Note> notes2chords(List<Note> notes, boolean isOn) throws Exception {
        List<Note> rightHandNotes = notes.stream().filter(note -> note.getNoteType().equals(NoteType.RIGHT_HAND_NOTE)).toList();
        List<Note> leftHandNotes = notes.stream().filter(note -> note.getNoteType().equals(NoteType.LEFT_HAND_BASS)).toList();
        if (notes.size() != rightHandNotes.size() + leftHandNotes.size()) {
            throw new Exception("Sizes are not equal");
        }
        List<Note> result = new ArrayList<>(rightHandNotes);
        SortedSet<Note> leftHandNotesSet = new TreeSet<>(leftHandNotes);
        Map<String, Note> leftHandNotesMap = leftHandNotesSet.stream().collect(Collectors.toMap(Note::getNoteName, Function.identity()));
        String leftHandNotesStr = toString(leftHandNotesSet);
        Map.Entry<String, String> chord = findChord(leftHandNotesStr);
        if (chord != null) {
            String[] splits = leftHandNotesStr.splitWithDelimiters(chord.getValue(), -1);
            for (String split : splits) {
                split = split.trim();
                if (!split.isEmpty()) {
                    if (split.equals(chord.getValue())) {
                        Note chordNote = new Note();
                        chordNote.setNoteType(NoteType.LEFT_HAND_CHORD);
                        chordNote.setNoteName(chord.getValue());
                        chordNote.setChordName(chord.getKey());
                        if (isOn) {
                            chordNote.setDuration(getChordDuration(chord.getValue(), leftHandNotesMap));
                        }
                        result.add(chordNote);
                    } else {
                        result.add(leftHandNotesMap.get(split));
                    }
                }
            }
        } else {
            result.addAll(leftHandNotes);
        }
        return result;
    }

    private int getChordDuration(String chord, Map<String, Note> notesMap) throws Exception {
        String[] splits = chord.split("\\s");
        Integer duration = notesMap.get(splits[0]).getDuration();
        for (String split : splits) {
            if (!notesMap.get(splits[0]).getDuration().equals(duration)) {
                throw new Exception("Unexpected chord duration");
            }
        }
        return duration;
    }

    private Map.Entry<String, String> findChord(String str) {
        for (Map.Entry<String, String> entry : Constants.NOTE_NAME_CHORD_MAP.entrySet()) {
            if (str.contains(entry.getValue())) {
                return entry;
            }
        }
        return null;
    }

    private String toString(SortedSet<Note> set) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Note note : set) {
            stringBuilder.append(note.getNoteName());
            stringBuilder.append(' ');
        }
        return stringBuilder.toString().trim();
    }

    /**
     * Octavian player works better when on and off points are not on the same points.
     * This method moves off notes one pixel and one millisecond back for such points.
     *
     * @param points
     */
    public IndexedTreeMap<Integer, Point> splitOnOffNotes(IndexedTreeMap<Integer, Point> points) {
        IndexedTreeMap<Integer, Point> result = new IndexedTreeMap<>();
        for (Map.Entry<Integer, Point> pointEntry : points.entrySet()) {
            Point point = pointEntry.getValue();
            if (!point.getNotesOn().isEmpty() && !point.getNotesOff().isEmpty()) {

                Point newOffPoint = new Point();
                newOffPoint.setTimestamp(point.getTimestamp() - 1);
                newOffPoint.setOffsetX(point.getOffsetX() - 1);
                newOffPoint.setBar(point.getBar());
                newOffPoint.setNotesOff(point.getNotesOff());

                Point newOnPoint = new Point();
                newOnPoint.setTimestamp(point.getTimestamp());
                newOnPoint.setOffsetX(point.getOffsetX());
                newOnPoint.setBar(point.getBar());
                newOnPoint.setNotesOn(point.getNotesOn());

                result.put(newOffPoint.getTimestamp(), newOffPoint);
                result.put(newOnPoint.getTimestamp(), newOnPoint);
            } else {
                result.put(pointEntry.getKey(), point);
            }
        }
        return result;
    }
}
