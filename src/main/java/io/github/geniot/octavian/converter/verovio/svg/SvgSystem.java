package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Data
@JacksonXmlRootElement(localName = "system")
public class SvgSystem {
    @JacksonXmlElementWrapper(localName = "path")
    BarPath barPath;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "measure")
    List<Measure> measures;

    public SortedSet<Integer> geBarOffsets() {
        SortedSet<Integer> barOffsets = new TreeSet<>();
        barOffsets.add(barPath.getBarOffset());
        for (Measure measure : measures) {
            barOffsets.add(measure.getBarOffset());
        }
        return barOffsets;
    }
}