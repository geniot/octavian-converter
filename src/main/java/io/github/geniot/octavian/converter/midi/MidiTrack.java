package io.github.geniot.octavian.converter.midi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MidiTrack {
    @JsonProperty(value = "name")
    String name;
    @JsonProperty(value = "notes")
    List<MidiNote> notes;
}
