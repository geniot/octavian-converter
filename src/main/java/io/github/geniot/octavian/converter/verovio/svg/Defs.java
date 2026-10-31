package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "defs")
public class Defs {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "g")
    List<G> gs;

    public String getPathByHref(String noteHeadHref) {
        for (G g : gs) {
            if (noteHeadHref.equals('#' + g.id)) {
                return g.path.d;
            }
        }
        return null;
    }
}
