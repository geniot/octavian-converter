package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;

@Data
@JacksonXmlRootElement(localName = "page_margin")
public class PageMargin {
    @JacksonXmlProperty(localName = "transform")
    String transform;
    @JacksonXmlElementWrapper(localName = "system")
    public SvgSystem svgSystem;

    /**
     * translate(260, 0)
     *
     * @return 260,0
     */
    public ImmutablePair<Long, Long> getTranslation() {
        String[] splits = transform.split(",");
        String one = splits[0].split("\\(")[1];
        String two = splits[1].split("\\)")[0];
        return new ImmutablePair<>(Long.parseLong(one.trim()), Long.parseLong(two.trim()));
    }
}
