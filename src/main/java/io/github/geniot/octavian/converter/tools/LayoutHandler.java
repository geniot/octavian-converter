package io.github.geniot.octavian.converter.tools;


import io.github.geniot.octavian.converter.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LayoutHandler {
    static Logger logger = LoggerFactory.getLogger(LayoutHandler.class);

    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final List<String> NOTE_NAMES_LIST = Arrays.asList(NOTE_NAMES);

    public void setFingers(List<Point> pointsList, Layout layout) throws Exception {
        removeLayout(pointsList, layout);
        for (int i = 0; i < layout.getLines().size(); i++) {
            String line = layout.getLines().get(i);
            SortedSet<Note> barNotes = getBarNotes(Layout.handByLayout(layout.getName()), pointsList, i + 1);
            List<Finger> barFingers = getBarFingers(line);
            if (barNotes.size() != barFingers.size()) {
                throw new Exception("Incorrect number of fingers for layout: " + layout.getName() +
                        "; line: " + (i + 1) +
                        "; fingers: " + barFingers.size() +
                        "; expected: " + barNotes.size() + "."
                );
            }
            int counter = 0;
            for (Note note : barNotes) {
                Map<String, Finger> fingersMap = note.getFingers();
                if (fingersMap == null) {
                    fingersMap = new HashMap<>();
                }
                Finger finger = barFingers.get(counter++);
                if (!finger.getFinger().equals("x") || finger.getButton() != null) {
                    fingersMap.put(layout.getName(), finger);
                    note.setFingers(fingersMap);
                }
            }
        }
    }

    private void removeLayout(List<Point> pointsList, Layout layout) {
        for (Point point : pointsList) {
            if (point.getNotesOn() != null) {
                for (Note note : point.getNotesOn()) {
                    if (note.getFingers() != null) {
                        note.getFingers().remove(layout.getName());
                    }
                }
            }
        }
    }

    protected List<Finger> getBarFingers(String line) throws NumberFormatException {
        line = line.replaceAll(" ", "");
        String[] splits = line.split("[\\[\\]]");
        boolean isFinger = true;
        List<Finger> fingers = new ArrayList<>();
        for (String split : splits) {
            if (isFinger) {
                StringBuilder stringBuffer = new StringBuilder();
                for (int i = 0; i < split.length(); i++) {
                    char c = split.charAt(i);
                    char nextC = i < split.length() - 1 ? split.charAt(i + 1) : c;
                    stringBuffer.append(c);
                    if (c != '_' && nextC != '_') {
                        Finger finger = new Finger(stringBuffer.toString());
                        fingers.add(finger);
                        stringBuffer.delete(0, stringBuffer.length());
                    }
                }
            } else {
                Finger finger = fingers.get(fingers.size() - 1);
                finger.setButton(Integer.parseInt(split));
            }
            isFinger = !isFinger;
        }
        return fingers;
    }

    private SortedSet<Note> getBarNotes(Set<NoteType> noteTypes, List<Point> pointList, int bar) {
        SortedSet<Note> barNotes = new TreeSet<>((o1, o2) -> {
            if (!Objects.equals(o1.getTimestamp(), o2.getTimestamp())) {
                return Integer.compare(o1.getTimestamp(), o2.getTimestamp());
            } else {
                int octave1 = Integer.parseInt(String.valueOf(o1.getNoteName().charAt(o1.getNoteName().length() - 1)));
                int octave2 = Integer.parseInt(String.valueOf(o2.getNoteName().charAt(o2.getNoteName().length() - 1)));
                if (octave1 != octave2) {
                    return Integer.compare(octave1, octave2);
                }

                String noteName1 = o1.getNoteName().substring(0, o1.getNoteName().length() - 1);
                String noteName2 = o2.getNoteName().substring(0, o2.getNoteName().length() - 1);

                int thisNoteNameIndex = NOTE_NAMES_LIST.indexOf(noteName1);
                int thatNoteNameIndex = NOTE_NAMES_LIST.indexOf(noteName2);
                return Integer.compare(thisNoteNameIndex, thatNoteNameIndex);
            }
        });
        List<Point> measurePoints = getMeasurePoints(pointList, bar);
        for (Point point : measurePoints) {
            for (Note note : point.getNotesOn()) {
                if (noteTypes.contains(note.getNoteType())) {
                    note.setTimestamp(point.getTimestamp());
                    barNotes.add(note);
                }
            }
        }
        return barNotes;
    }

    private List<Point> getMeasurePoints(List<Point> pointsList, int measure) {
        List<Point> result = new ArrayList<>();
        for (Point point : pointsList) {
            if (point.getBar() == measure && point.getNotesOn() != null) {
                result.add(point);
            }
        }
        return result;
    }

    public void mergeFingering(Tune playerTune, Tune repeatsTune, int[] repeats) throws Exception {

        List<Point> playerTunePoints = Arrays.asList(playerTune.getPoints());
        List<Point> repeatTunePoints = Arrays.asList(repeatsTune.getPoints());

        for (int i = 0; i < repeats.length; i++) {
            List<Point> repeatsMeasurePoints = getMeasurePoints(repeatTunePoints, repeats[i]);
            List<Point> playerMeasurePoints = getMeasurePoints(playerTunePoints, i + 1);

            if (repeatsMeasurePoints.size() != playerMeasurePoints.size()) {
                throw new Exception("Measures are not equal in points length: " + i);
            }

            for (int k = 0; k < playerMeasurePoints.size(); k++) {
                Point playerPoint = playerMeasurePoints.get(k);
                Point repeatPoint = repeatsMeasurePoints.get(k);
                mergeFingers(playerPoint, repeatPoint);
            }
        }
    }

    public Map<String, Layout> extractFingerings(Tune tune) {
        Map<String, Layout> map = new HashMap<>();
        return map;
    }

    /**
     * Optimistic merge without sizes check.
     *
     * @param nextTune
     * @param beforeTune
     * @throws Exception
     */
    public void mergeFingering(Tune nextTune, Tune beforeTune) throws Exception {

        List<Point> nextTunePoints = Arrays.asList(nextTune.getPoints());
        List<Point> beforeTunePoints = Arrays.asList(beforeTune.getPoints());

        for (int k = 0; k < nextTunePoints.size(); k++) {
            Point nextPoint = nextTunePoints.get(k);
            if (nextPoint.getNotesOn() != null) {
                Point beforePoint = beforeTunePoints.get(k);
                mergeFingers(nextPoint, beforePoint);
            }
        }
    }

    private void mergeFingers(Point toPoint, Point fromPoint) throws Exception {
        if (toPoint.getNotesOn().length != fromPoint.getNotesOn().length) {
            throw new Exception("Notes not equal.");
        }
        for (int i = 0; i < toPoint.getNotesOn().length; i++) {
            Note toNote = toPoint.getNotesOn()[i];
            Note fromNote = fromPoint.getNotesOn()[i];
            if (!toNote.getNoteType().equals(fromNote.getNoteType())) {
                throw new Exception("Note types are not equal: " + toNote.getNoteType() + " != " + fromNote.getNoteType());
            }
            toNote.setFingers(fromNote.getFingers());
        }
    }
}
