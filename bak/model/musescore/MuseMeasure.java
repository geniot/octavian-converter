package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MuseMeasure {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "voice")
    List<MuseVoice> voices;

    public void setVoices(List<MuseVoice> value) {
        if (voices == null) {
            voices = new ArrayList<>(value.size());
        }
        voices.addAll(value);
    }
}
