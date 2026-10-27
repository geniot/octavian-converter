package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MuseStaff {

    @JacksonXmlProperty(localName = "id", isAttribute = true)
    int id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Measure")
    List<MuseMeasure> measures;

    public void setMeasures(List<MuseMeasure> value) {
        if (measures == null) {
            measures = new ArrayList<>(value.size());
        }
        measures.addAll(value);
    }
}
