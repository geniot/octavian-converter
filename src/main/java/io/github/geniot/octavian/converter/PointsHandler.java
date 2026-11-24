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

import static io.github.geniot.octavian.converter.Constants.CHORDS_MAP;
import static io.github.geniot.octavian.converter.Constants.CHORD_VALUES_MAP;

public class PointsHandler {

    public void addPoints(IndexedTreeMap<Integer, Point> points,
                          MidiRoot midiRoot,
                          SortedMap<Integer, Set<String>> noteOffsets) throws Exception {
        for (int i = 0; i < midiRoot.getTracks().size(); i++) {
            MidiTrack track = midiRoot.getTracks().get(i);
            for (MidiNote midiNote : track.getNotes()) {
                int duration = (int) (midiNote.getDuration() * 1000);
                int onTimestamp = (int) (midiNote.getTime() * 1000);
                int offTimestamp = onTimestamp + duration;
                int midiValue = midiNote.getMidi();

                Point onPoint = points.getOrDefault(onTimestamp, new Point());
                setOffsetX(onPoint, findById(noteOffsets, midiNote.getId()));
                onPoint.setTimestamp(onTimestamp);
                Note onNote = new Note();
                onNote.setId(midiNote.getId());
                onNote.setNoteType(i == 0 ? NoteType.RIGHT_HAND_NOTE : NoteType.LEFT_HAND_BASS);
                onNote.setDuration(duration);
                onNote.setNoteValue(midiValue);
                onNote.setNoteName(Constants.NOTES_MAP.get(midiValue));
                onPoint.getNotesOn().add(onNote);

                Point offPoint = points.getOrDefault(offTimestamp, new Point());
                offPoint.setTimestamp(offTimestamp);
                Note offNote = new Note();
                offNote.setId(midiNote.getId());
                offNote.setNoteType(i == 0 ? NoteType.RIGHT_HAND_NOTE : NoteType.LEFT_HAND_BASS);
                offNote.setNoteValue(midiValue);
                offNote.setNoteName(Constants.NOTES_MAP.get(midiValue));
                offPoint.getNotesOff().add(offNote);

                points.put(onTimestamp, onPoint);
                points.put(offTimestamp, offPoint);
            }
        }
    }

    private Integer findById(SortedMap<Integer, Set<String>> noteOffsets, String id) {
        for (Map.Entry<Integer, Set<String>> entry : noteOffsets.entrySet()) {
            if (entry.getValue().contains(id)) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Couldn't find offset by id: " + id);
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
            point.setNotesOn(notes2chords(point.getNotesOn(), true, point.getOffsetX()));
            point.setNotesOff(notes2chords(point.getNotesOff(), false, point.getOffsetX()));
        }
    }

    private List<Note> notes2chords(List<Note> notes, boolean isOn, int offsetX) throws Exception {
        List<Note> rightHandNotes = new ArrayList<>(notes.stream().filter(note -> note.getNoteType().equals(NoteType.RIGHT_HAND_NOTE)).toList());
        List<Note> leftHandNotes = new ArrayList<>(notes.stream().filter(note -> note.getNoteType().equals(NoteType.LEFT_HAND_BASS)).toList());
        rightHandNotes.sort(Comparator.comparing(Note::getNoteValue));
        leftHandNotes.sort(Comparator.comparing(Note::getNoteValue));

        if (notes.size() != rightHandNotes.size() + leftHandNotes.size()) {
            throw new Exception("Sizes are not equal");
        }
        List<Note> result = new ArrayList<>(rightHandNotes);

        if (leftHandNotes.size() >= 3) {
            String noteValues = toNotesString(leftHandNotes);
            String chordStr = findChord(noteValues);
            Chord chord = CHORD_VALUES_MAP.get(chordStr);
            if (chordStr != null && chord != null) {
                Map<Integer, Note> leftHandNotesMap = leftHandNotes.stream().collect(Collectors.toMap(Note::getNoteValue, Function.identity()));
                Note chordNote = new Note();
                chordNote.setNoteType(NoteType.LEFT_HAND_CHORD);
                chordNote.setNoteName(CHORDS_MAP.get(chord));
                chordNote.setChordName(chord.getChordName());
                if (isOn) {
                    List<Note> chordNotes = getNotesFromString(chordStr, leftHandNotesMap);
                    chordNote.setDuration(getChordDuration(chordNotes, offsetX));
                }
                result.add(chordNote);
                List<Note> remainingNotes = getNotesFromString(noteValues.substring(chordStr.length()), leftHandNotesMap);
                result.addAll(remainingNotes);
            } else {
                throw new RuntimeException("Couldn't detect the chord at " + offsetX);
//                result.addAll(leftHandNotes);
            }


        } else {
            result.addAll(leftHandNotes);
        }
        return result;
    }

    private List<Note> getNotesFromString(String str, Map<Integer, Note> notesMap) {
        List<Note> result = new ArrayList<>();
        String[] values = str.split("\\s");
        for (String value : values) {
            if (!value.isEmpty()) {
                result.add(notesMap.get(Integer.parseInt(value)));
            }
        }
        return result;
    }

    private String findChord(String noteValues) {
        for (Map.Entry<String, Chord> entry : CHORD_VALUES_MAP.entrySet()) {
            if (noteValues.startsWith(entry.getKey())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String toNotesString(List<Note> notesSet) {
        StringBuilder stringBuilder = new StringBuilder();
        notesSet.reversed().forEach((note) -> {
            stringBuilder.append(note.getNoteValue());
            stringBuilder.append(' ');
        });
        return stringBuilder.toString().trim();
    }

    private int getChordDuration(List<Note> notes, int offsetX) throws Exception {
        Integer duration = notes.getFirst().getDuration();
        for (Note note : notes) {
            if (!note.getDuration().equals(duration)) {
                System.out.println("Unexpected chord duration at " + offsetX + "; " +
                        notes.stream().map(Note::getNoteName).toList() + "; " +
                        notes.stream().map(Note::getDuration).toList());
                duration = Math.min(duration, note.getDuration());
            }
        }
        return duration;
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
