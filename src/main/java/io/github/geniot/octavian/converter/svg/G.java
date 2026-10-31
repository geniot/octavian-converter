package io.github.geniot.octavian.converter.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "g")
public class G {
    @JacksonXmlElementWrapper(localName = "id")
    public String id;
    @JacksonXmlElementWrapper(localName = "path")
    public Path path;
}
