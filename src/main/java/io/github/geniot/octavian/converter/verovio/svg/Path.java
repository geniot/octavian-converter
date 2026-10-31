package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "path")
public class Path {
    @JacksonXmlElementWrapper(localName = "d")
    public String d;
}
