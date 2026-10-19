package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "svg")
public class _0SvgRoot {

    @JacksonXmlElementWrapper(localName = "svg")
    _1DefinitionScale a1DefinitionScale;
}