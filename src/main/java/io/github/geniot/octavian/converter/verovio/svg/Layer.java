package io.github.geniot.octavian.converter.verovio.svg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
}
