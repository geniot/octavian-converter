package io.github.geniot.octavian.converter.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "chord")
public class Chord {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "note")
    List<SvgNote> notes;
}
