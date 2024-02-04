package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.model.Note;
import io.github.geniot.octavian.converter.model.NoteType;
import io.github.geniot.octavian.converter.model.Point;
import lombok.Data;
import org.apache.commons.lang3.SerializationUtils;

import java.util.*;

@Data
public class MuseChord implements Comparable<MuseChord>, IMuseMeasurable {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Note")
    List<MuseNote> museNotes;
    @JacksonXmlProperty(localName = "durationType")
    String durationType;

    int measure;
    int orderNum;
    /**
     * In a rare case (more than one voice in one staff) we need to merge and sort chords by local measure timestamp,
     * rather than by the order provided in the musescore file. We need to match the order of midi events.
     */
    double localMeasureTimestamp;

    public void setMuseNotes(List<MuseNote> value) {
        if (museNotes == null) {
            museNotes = new ArrayList<>(value.size());
        }
        museNotes.addAll(value);
    }

    public boolean isAudible() {
        for (MuseNote museNote : museNotes) {
            if (museNote.isAudible()) {
                return true;
            }
        }
        return false;
    }

    public IndexedTreeMap<Integer, Point> getPoints(boolean isOn, float ratio, NoteType noteType, String fingeringLayoutName) {
        IndexedTreeMap<Integer, Point> points = new IndexedTreeMap<>();
        for (MuseNote museNote : museNotes) {
            if (museNote.isAudible()) {
                int timestamp = isOn ? museNote.getTickOn().intValue() : museNote.getTickOff().intValue();
                Point point = points.get(timestamp);
                if (point == null) {
                    point = new Point();
                    point.setTimestamp(timestamp);
                    point.setOffsetX(isOn ? getWeightedOffsetX(ratio) : -1);//isOff case will be calculated later
                }
                Note[] notes = isOn ? point.getNotesOn() : point.getNotesOff();
                List<Note> notesList = new ArrayList<>(Arrays.asList(notes == null ? new Note[]{} : notes));
                notesList.add(museNote.toNote(isOn, noteType, fingeringLayoutName));
                if (isOn) {
                    point.setNotesOn(notesList.toArray(new Note[0]));
                } else {
                    point.setNotesOff(notesList.toArray(new Note[0]));
                }
                points.put(timestamp, point);
            }
        }
        return points;
    }

    /**
     * In rare cases one note in a chord has a different offsetX, we need to find the main offsetX.
     *
     * @return
     */
    private int getWeightedOffsetX(float ratio) {
        Map<Integer, Integer> weightedOffsets = new HashMap<>();
        for (MuseNote museNote : museNotes) {
            if (museNote.isAudible()) {
                int offsetX = (int) (museNote.svgNote.getX() / ratio);
                Integer weight = weightedOffsets.get(offsetX);
                weight = weight == null ? 0 : weight;
                weightedOffsets.put(offsetX, ++weight);
            }
        }
        //https://stackoverflow.com/questions/5911174/finding-key-associated-with-max-value-in-a-java-map
        return Collections.max(weightedOffsets.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }

    @Override
    public int compareTo(MuseChord o) {
        return Double.compare(localMeasureTimestamp, o.localMeasureTimestamp);
    }

    public List<MuseNote> getMuseSpannerNotes() {
        List<MuseNote> mNotes = new ArrayList<>();
        for (MuseNote museNote : museNotes) {
            mNotes.add(museNote);
            if (museNote.getSpanners() != null && museNote.getSpanners().size() > 1) {
                for (int i = 1; i < museNote.getSpanners().size(); i++) {
                    mNotes.add(SerializationUtils.clone(museNote));
                }
            }
        }
        return mNotes;
    }
}
