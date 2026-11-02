package io.github.geniot.octavian.converter.json;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@JsonPropertyOrder({
        "duration",
        "noteType",
        "noteValue",
        "noteName",
        "chordName",
        "fingers"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Note implements Serializable {
    Integer duration;
    NoteType noteType;
    Integer noteValue;
    String noteName;
    String chordName;
    Map<String, Finger> fingers;
}
