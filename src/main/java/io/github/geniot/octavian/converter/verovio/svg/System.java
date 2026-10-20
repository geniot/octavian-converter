package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "system")
public class System {
    @JacksonXmlElementWrapper(localName = "path")
    BarPath barPath;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "measure")
    List<Measure> measures;
}