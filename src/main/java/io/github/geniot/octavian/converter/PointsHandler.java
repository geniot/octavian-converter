package io.github.geniot.octavian.converter;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.json.Note;
import io.github.geniot.octavian.converter.json.Point;
import io.github.geniot.octavian.converter.json.Tune;
import io.github.geniot.octavian.converter.midi.MidiNote;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import io.github.geniot.octavian.converter.midi.MidiTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;

public class PointsHandler {

    public void addPoints(IndexedTreeMap<Integer, Point> points,
                          MidiRoot midiRoot,
                          Map<String, Integer> noteOffsets) throws Exception {
        for (MidiTrack track : midiRoot.getTracks()) {
            for (MidiNote note : track.getNotes()) {
                int duration = (int) (note.getDuration() * 1000);
                int onTime = (int) (note.getTime() * 1000);
                int offTime = onTime + duration;
                int value = note.getMidi();

                Point onPoint = points.getOrDefault(onTime, new Point());
                setOffsetX(onPoint, note, noteOffsets);
                onPoint.setTimestamp(onTime);
                Note onNote = new Note();
                onNote.setDuration(duration);
                onNote.setNoteValue(value);
                onNote.setNoteName(Constants.NOTE_NAME_MAP.get(value));
                onPoint.getNotesOn().add(onNote);

                Point offPoint = points.getOrDefault(offTime, new Point());
                offPoint.setTimestamp(offTime);
                Note offNote = new Note();
                offNote.setNoteValue(value);
                offNote.setNoteName(Constants.NOTE_NAME_MAP.get(value));
                offPoint.getNotesOff().add(offNote);

                points.put(onTime, onPoint);
                points.put(offTime, offPoint);
            }
        }
    }

    private void setOffsetX(Point onPoint, MidiNote note, Map<String, Integer> noteOffsets) throws Exception {
        Integer offset = noteOffsets.get(note.getId());
        if (offset == null) {
            throw new Exception("Note id not found: " + note.getId());
        }
        if (onPoint.getOffsetX() != null && !onPoint.getOffsetX().equals(offset)) {
            throw new Exception("Offset X or Offset X not equal: " + onPoint.getOffsetX());
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

    public void setBars(IndexedTreeMap<Integer, Point> points, SortedSet<Integer> barOffsets) {
        for (Point point : points.values()) {
            int insertionPoint = Arrays.binarySearch(barOffsets.toArray(), point.getOffsetX());
            insertionPoint = insertionPoint < 0 ? Math.abs(insertionPoint) - 1 : insertionPoint;
            point.setBar(insertionPoint - 1);
        }
    }

    public void scale(Tune tune, float scaleFactor) {
        for (int i = 0; i < tune.getBarOffsets().length; i++) {
            tune.getBarOffsets()[i] /= (int) scaleFactor;
        }
    }

}
