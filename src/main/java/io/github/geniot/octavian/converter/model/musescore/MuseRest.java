package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class MuseRest implements IMuseMeasurable{
    @JacksonXmlProperty(localName = "durationType")
    String durationType;

    int orderNum;
}
