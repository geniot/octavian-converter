package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Fingering implements Serializable {
    @JacksonXmlProperty(localName = "text")
    String text;
}
