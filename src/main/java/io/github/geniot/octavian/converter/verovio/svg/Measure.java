package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "g")
public class Measure {
    @JacksonXmlProperty(localName = "class")
    String cl;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "g")
    List<MeasureItem> measureItems;
}
