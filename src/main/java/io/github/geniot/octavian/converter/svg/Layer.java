package io.github.geniot.octavian.converter.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JacksonXmlRootElement(localName = "layer")
public class Layer {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "note")
    List<SvgNote> notes = new ArrayList<>();
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "beam")
    List<Beam> beams = new ArrayList<>();
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "chord")
    List<Chord> chords = new ArrayList<>();

    public Map<String, Integer> getNoteOffsets() {
        Map<String, Integer> noteOffsets = new HashMap<>();
        for (SvgNote note : notes) {
            noteOffsets.put(note.id, note.noteHead.use.getOffset());
        }
        for (Beam beam : beams) {
            for (SvgNote note : beam.notes) {
                noteOffsets.put(note.id, note.noteHead.use.getOffset());
            }
        }
        for (Chord chord : chords) {
            for (SvgNote note : chord.notes) {
                noteOffsets.put(note.id, note.noteHead.use.getOffset());
            }
        }
        return noteOffsets;
    }

    public Map<String, Use> getNoteHeadHrefs() {
        Map<String, Use> noteHeadHrefs = new HashMap<>();
        for (SvgNote note : notes) {
            noteHeadHrefs.put(note.noteHead.use.getXlinkHref(), note.noteHead.use);
        }
        for (Beam beam : beams) {
            for (SvgNote note : beam.notes) {
                noteHeadHrefs.put(note.noteHead.use.getXlinkHref(), note.noteHead.use);
            }
        }
        for (Chord chord : chords) {
            for (SvgNote note : chord.notes) {
                noteHeadHrefs.put(note.noteHead.use.getXlinkHref(), note.noteHead.use);
            }
        }
        return noteHeadHrefs;
    }
}
