package io.github.geniot.octavian.converter.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.*;

@Data
@JacksonXmlRootElement(localName = "layer")
public class Layer {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "note")
    List<Note> notes = new ArrayList<>();
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "beam")
    List<Beam> beams = new ArrayList<>();
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "chord")
    List<Chord> chords = new ArrayList<>();

    public SortedSet<Integer> getNoteOffsets() {
        SortedSet<Integer> noteOffsets = new TreeSet<>();
        for (Note note : notes) {
            noteOffsets.add(note.noteHead.use.getOffset());
        }
        for (Beam beam : beams) {
            for (Note note : beam.notes) {
                noteOffsets.add(note.noteHead.use.getOffset());
            }
        }
        for (Chord chord : chords) {
            for (Note note : chord.notes) {
                noteOffsets.add(note.noteHead.use.getOffset());
            }
        }
        return noteOffsets;
    }

    public Map<String,Use> getNoteHeadHrefs() {
        Map<String,Use> noteHeadHrefs = new HashMap<>();
        for (Note note : notes) {
            noteHeadHrefs.put(note.noteHead.use.getXlinkHref(),note.noteHead.use);
        }
        for (Beam beam : beams) {
            for (Note note : beam.notes) {
                noteHeadHrefs.put(note.noteHead.use.getXlinkHref(),note.noteHead.use);
            }
        }
        for (Chord chord : chords) {
            for (Note note : chord.notes) {
                noteHeadHrefs.put(note.noteHead.use.getXlinkHref(),note.noteHead.use);
            }
        }
        return noteHeadHrefs;
    }
}
