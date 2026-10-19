package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "g")
public class _3System {
    @JacksonXmlElementWrapper(localName = "path")
    BarPath barPath;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "g")
    List<Measure> measures;
}