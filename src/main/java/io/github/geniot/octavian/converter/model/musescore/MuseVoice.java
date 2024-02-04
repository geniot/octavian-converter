package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class MuseVoice {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Chord")
    List<MuseChord> chords;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Rest")
    List<MuseRest> rests;

    int orderCounter = 0;

    public List<IMuseMeasurable> getMeasurables() {
        List<IMuseMeasurable> measurables = new ArrayList<>();
        if (chords != null) {
            measurables.addAll(chords);
        }
        if (rests != null) {
            measurables.addAll(rests);
        }
        Collections.sort(measurables, new Comparator<IMuseMeasurable>() {
            @Override
            public int compare(IMuseMeasurable o1, IMuseMeasurable o2) {
                return Integer.compare(o1.getOrderNum(), o2.getOrderNum());
            }
        });
        return measurables;
    }

    public void setChords(List<MuseChord> value) {
        if (chords == null) {
            chords = new ArrayList<>(value.size());
        }
        for (MuseChord v : value) {
            v.setOrderNum(orderCounter++);
            chords.add(v);
        }
    }

    public void setRests(List<MuseRest> value) {
        if (rests == null) {
            rests = new ArrayList<>(value.size());
        }
        for (MuseRest v : value) {
            v.setOrderNum(orderCounter++);
            rests.add(v);
        }
    }

}
