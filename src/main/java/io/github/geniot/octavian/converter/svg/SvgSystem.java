package io.github.geniot.octavian.converter.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.*;

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

    public SortedSet<Integer> geNoteOffsets() {
        SortedSet<Integer> noteOffsets = new TreeSet<>();
        for (Measure measure : measures) {
            noteOffsets.addAll(measure.getNoteOffsets());
        }
        return noteOffsets;
    }

    public Map<String,Use> getNoteHeadHrefs() {
        Map<String,Use> noteHeadHrefs = new HashMap<>();
        for (Measure measure : measures) {
            noteHeadHrefs.putAll(measure.getNoteHeadHrefs());
        }
        return noteHeadHrefs;
    }
}