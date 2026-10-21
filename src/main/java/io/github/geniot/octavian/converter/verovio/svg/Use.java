package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "use")
public class Use {
    @JacksonXmlElementWrapper(localName = "transform")
    String transform;

    /**
     * translate(2266, 810) scale(0.72, 0.72)
     *
     * @return 2266
     */
    public Integer getOffset() {
        return Integer.parseInt(transform.split(",")[0].split("\\(")[1]);
    }
}
