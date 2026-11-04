package io.github.geniot.octavian.converter.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Finger implements Serializable {
    String finger;
    Integer button;

    public Finger(String finger, Integer button) {
        this.finger = finger;
        this.button = button;
    }
}
