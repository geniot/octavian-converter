package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "definition_scale")
public class DefinitionScale {

    @JacksonXmlElementWrapper(localName = "page_margin")
    public PageMargin pageMargin;
}