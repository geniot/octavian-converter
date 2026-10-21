package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.awt.*;

@Data
@JacksonXmlRootElement(localName = "definition_scale")
public class DefinitionScale {

    @JacksonXmlElementWrapper(localName = "page_margin")
    public PageMargin pageMargin;

    @JacksonXmlProperty(localName = "viewBox")
    String viewBox;

    public Rectangle getViewBox() {
        String[] splits = viewBox.split("\\s");
        return new Rectangle(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2]), Integer.parseInt(splits[3]));
    }
}