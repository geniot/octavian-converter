package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MuseLocation implements Serializable {
    @JacksonXmlProperty(localName = "measures")
    String measures;
    @JacksonXmlProperty(localName = "fractions")
    String fractions;
}
