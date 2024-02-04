package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MuseSpanner implements Serializable {
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    String type;
    @JacksonXmlProperty(localName = "prev")
    MusePrevNext prev;
    @JacksonXmlProperty(localName = "next")
    MusePrevNext next;
}
