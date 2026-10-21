package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "path")
public class BarPath {
    @JacksonXmlProperty(localName = "d")
    String d;

    /**
     * M13 540 L13 3060 -> 13
     * @return
     */
    public Integer getBarOffset() {
        return Integer.parseInt(d.split(" ")[0].substring(1));
    }
}
