package io.github.geniot.octavian.converter.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.*;

@Data
@JacksonXmlRootElement(localName = "measure")
public class Measure {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "staff")
    List<Staff> staffs;

    @JacksonXmlElementWrapper(localName = "barLine")
    public BarLine barLine;

    public Integer getBarOffset() {
        return barLine.getBarOffset();
    }

    public SortedSet<Integer> getNoteOffsets() {
        SortedSet<Integer> noteOffsets = new TreeSet<>();
        for (Staff staff : staffs) {
            noteOffsets.addAll(staff.layer.getNoteOffsets());
        }
        return noteOffsets;
    }

    public Map<String,Use> getNoteHeadHrefs() {
        Map<String,Use> noteHeadHrefs = new HashMap<>();
        for (Staff staff : staffs) {
            noteHeadHrefs.putAll(staff.layer.getNoteHeadHrefs());
        }
        return noteHeadHrefs;
    }
}
