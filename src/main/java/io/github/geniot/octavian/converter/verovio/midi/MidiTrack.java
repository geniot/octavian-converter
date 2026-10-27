package io.github.geniot.octavian.converter.verovio.midi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MidiTrack {
    @JsonProperty(value = "name")
    String name;
    @JsonProperty(value = "notes")
    List<MidiNote> notes;
}
