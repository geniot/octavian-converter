package io.github.geniot.octavian.converter.midi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;

@Data
@JsonRootName(value = "header")
public class MidiRoot {

    @JsonProperty(value = "tracks")
    List<MidiTrack> tracks;
}
