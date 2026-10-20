package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "layer")
public class Layer {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "note")
    List<Note> notes;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "beam")
    List<Beam> beams;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "chord")
    List<Chord> chords;
}
