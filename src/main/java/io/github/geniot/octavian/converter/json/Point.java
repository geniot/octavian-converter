package io.github.geniot.octavian.converter.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonPropertyOrder({
        "timestamp",
        "offsetX",
        "bar",
        "notesOn",
        "notesOff"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Point implements Serializable {
    int timestamp;
    int offsetX;
    int bar;

    List<Note> notesOn = new ArrayList<>();
    List<Note> notesOff = new ArrayList<>();
}
