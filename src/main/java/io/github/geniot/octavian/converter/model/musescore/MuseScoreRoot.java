package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.indexedtreemap.IndexedTreeSet;
import io.github.geniot.octavian.converter.model.NoteType;
import io.github.geniot.octavian.converter.model.Point;
import io.github.geniot.octavian.converter.model.SvgNote;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.ShortMessage;
import java.util.*;

import static io.github.geniot.octavian.converter.model.Constants.DURATIONS_MAP;
import static io.github.geniot.octavian.converter.tools.PointsHandler.mergePointMaps;


@Data
@JacksonXmlRootElement(localName = "museScore")
public class MuseScoreRoot {
    static Logger logger = LoggerFactory.getLogger(MuseScoreRoot.class);

    @JacksonXmlElementWrapper(localName = "Score")
    MuseScore score;

    List<MuseNote> audibleNotesList;
    IndexedTreeSet<MuseNote> allNotesSet;
    List<MuseChord> allChordsList;

    public void registerTick(long millisecond, int pitch, int messageCommand) throws Exception {
//        logger.info("Register tick:" + millisecond + ";" + pitch + ";" + messageCommand);
        List<MuseNote> notes = getAudibleNotes();
        for (MuseNote museNote : notes) {

//            if (museNote.getPitch() != pitch &&
//                    museNote.getTickOn() == null &&
//                    museNote.getTickOff() == null
//            ) {
//                logger.debug("Skipping at measure: " + museNote.getMeasure());
//            }

            if (museNote.isAudible() && museNote.getPitch() == pitch) {
                if (messageCommand == ShortMessage.NOTE_ON) {
                    if (museNote.getTickOn() == null) {
                        museNote.setTickOn(millisecond);
                        return;
                    }
                } else if (messageCommand == ShortMessage.NOTE_OFF) {
                    if (museNote.getTickOn() != null && museNote.getTickOff() == null) {
                        museNote.setTickOff(millisecond);
                        return;
                    }
                } else {
                    throw new Exception("Unidentified messageCommand: " + messageCommand);
                }
            }
        }
        if (messageCommand == ShortMessage.NOTE_OFF) {
            logger.warn("Couldn't find an off-note for " + millisecond + ";" + pitch + ";" + messageCommand);
        }else{
            throw new Exception("Couldn't find a on-note for " + millisecond + ";" + pitch + ";" + messageCommand);
        }
    }

    private List<MuseNote> getAudibleNotes() {
        if (audibleNotesList == null) {
            audibleNotesList = getNotes(true);
        }
        return audibleNotesList;
    }

    private IndexedTreeSet<MuseNote> getAllNotes() {
        if (allNotesSet == null) {
            allNotesSet = new IndexedTreeSet<>();
            allNotesSet.addAll(getNotes(false));
        }
        return allNotesSet;
    }

    private List<MuseChord> getAllChords() {
        if (allChordsList == null) {
            allChordsList = new ArrayList<>();
            for (MuseStaff museStaff : score.getStaffs()) {
                int measureNum = 1;
                for (MuseMeasure measure : museStaff.getMeasures()) {
                    List<MuseChord> chords = new ArrayList<>();
                    for (MuseVoice voice : measure.getVoices()) {
                        if (voice.getChords() != null) {//can be empty when there are only pause marks in a measure

                            double localMeasureTimestamp = 0;

                            for (IMuseMeasurable measurable : voice.getMeasurables()) {
                                if (measurable instanceof MuseChord) {
                                    MuseChord museChord = (MuseChord) measurable;
                                    museChord.setLocalMeasureTimestamp(localMeasureTimestamp);
                                    chords.add(museChord);
                                    localMeasureTimestamp += DURATIONS_MAP.get(museChord.durationType);
                                } else {
                                    MuseRest museRest = (MuseRest) measurable;
                                    localMeasureTimestamp += DURATIONS_MAP.get(museRest.durationType);
                                }
                            }
                        }
                    }
                    int finalMeasureNum = measureNum;
                    chords.forEach(museChord -> museChord.setMeasure(finalMeasureNum));
                    if (measure.getVoices().size() > 1) {
                        Collections.sort(chords);
                    }
                    allChordsList.addAll(chords);
                    ++measureNum;
                }
            }
        }
        return allChordsList;
    }

    private List<MuseNote> getNotes(boolean shouldFilterAudible) {
        List<MuseNote> notesList = new ArrayList<>();
        List<MuseChord> chordsList = getAllChords();
        for (MuseChord chord : chordsList) {
            for (MuseNote museNote : chord.getMuseSpannerNotes()) {
                museNote.setMeasure(chord.getMeasure());
                if (!shouldFilterAudible) {
                    notesList.add(museNote);
                } else if (museNote.isAudible()) {
                    notesList.add(museNote);
                }
            }
        }
        return notesList;
    }

    /**
     * Calculated ticks in silent notes are necessary to correctly map them with svg notes.
     */
    public void setTicksToSilentNotes() {
        List<MuseNote> notesList = getNotes(false);
        for (int i = 1; i < notesList.size(); i++) {
            MuseNote thisMuseNote = notesList.get(i);
            if (thisMuseNote.getTickOn() == null && thisMuseNote.getTickOff() == null) {
                if (thisMuseNote.isTie()) {
                    MuseNote prevMuseNote = notesList.get(i - 1);
                    long delta = Math.abs(prevMuseNote.getTickOn() - prevMuseNote.getTickOff());

                    thisMuseNote.setTickOn(prevMuseNote.getTickOn() + delta);
                    thisMuseNote.setTickOff(prevMuseNote.getTickOn() + delta);
                }
            }
        }
    }

    /**
     * After ticks are set we need to validate the notes.
     */
    public void validateTicks() throws Exception {
        List<MuseNote> notes = getAudibleNotes();
        for (MuseNote note : notes) {
            if (note.isAudible()) {
                if (note.getTickOn() == null || note.getTickOff() == null) {
                    throw new Exception("Note is not valid: " + note.getPitch());
                }
            } else {
                if (note.getTickOn() != null || note.getTickOff() != null) {
                    if (note.isDurable()) {
                        throw new Exception("Found an invalid note: " + note.getPitch());
                    }
                }
            }
        }
        //checking that notes in every chord have the same tickOn timestamp
        List<MuseChord> chordsList = getAllChords();
        for (MuseChord chord : chordsList) {
            Set<Long> tickOnSet = new HashSet<>();
            for (MuseNote museNote : chord.getMuseNotes()) {
                if (museNote.getTickOn() != null) {
                    tickOnSet.add(museNote.getTickOn());
                }
            }
            if (tickOnSet.size() > 1) {
                logger.warn("Found chord notes with different timestamps, measure: " + chord.getMeasure());
            }
        }
    }

    public void setSvgNotes(IndexedTreeSet<SvgNote> svgNotes) throws Exception {
        IndexedTreeSet<MuseNote> notes = getAllNotes();

        //exceptional case, collecting debug info
        if (notes.size() != svgNotes.size()) {
            //comparing notes count in measures
            for (int i = 0; i < notes.size(); i++) {
                MuseNote note = notes.exact(i);
                int measure = note.getMeasure();
                int museNotesMeasureCount = getMuseNotesCount(notes, measure);
                int svgNotesMeasureCount = getSvgNotesCount(svgNotes, measure);
                if (museNotesMeasureCount != svgNotesMeasureCount) {
                    logger.warn("Notes count not equal at measure:" + measure);
                }
            }

            //trying to detect the place where pitch goes up and y goes down
            int pitchDirection = 0;
            int yDirection = 0;
            int pitchValue = 0;
            float yValue = Float.MAX_VALUE;

            for (int i = 0; i < notes.size(); i++) {
                MuseNote note = notes.exact(i);
                if (note.getPitch() > pitchValue) {
                    pitchDirection = 1;
                } else if (note.getPitch() < pitchValue) {
                    pitchDirection = -1;
                } else {
                    pitchDirection = 0;
                }
                SvgNote svgNote = svgNotes.exact(i);
                if (svgNote.getY() > yValue) {
                    yDirection = -1;
                } else if (svgNote.getY() < yValue) {
                    yDirection = 1;
                } else {
                    yDirection = 0;
                }
                pitchValue = note.getPitch();
                yValue = svgNote.getY();

                if (pitchDirection != yDirection) {
                    logger.warn("Direction different at measure:" + note.getMeasure());
                }

            }
            throw new Exception("SvgNotes size is not the same as MuseNotes:" + svgNotes.size() + ";" + notes.size());
        } else {
            //else everything is fine
            for (int i = 0; i < notes.size(); i++) {
                notes.exact(i).setSvgNote(svgNotes.exact(i));
            }
        }
    }

    private int getSvgNotesCount(IndexedTreeSet<SvgNote> svgNotes, int measure) {
        int count = 0;
        for (int i = 0; i < svgNotes.size(); i++) {
            count += svgNotes.exact(i).getMeasure() == measure ? 1 : 0;
        }
        return count;
    }

    private int getMuseNotesCount(IndexedTreeSet<MuseNote> notes, int measure) {
        int count = 0;
        for (int i = 0; i < notes.size(); i++) {
            count += notes.exact(i).getMeasure() == measure ? 1 : 0;
        }
        return count;
    }

    public IndexedTreeMap<Integer, Point> getPoints(float ratio, NoteType noteType, String fingeringLayoutName) {
        IndexedTreeMap<Integer, Point> points = new IndexedTreeMap<>();
        List<MuseChord> chordsList = getAllChords();
        for (MuseChord chord : chordsList) {
            mergePointMaps(points, chord.getPoints(true, ratio, noteType, fingeringLayoutName));
            mergePointMaps(points, chord.getPoints(false, ratio, noteType, fingeringLayoutName));
        }
        return points;
    }
}
