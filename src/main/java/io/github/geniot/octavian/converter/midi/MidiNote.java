package io.github.geniot.octavian.converter.midi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *           "durationTicks": 227,
 *           "midi": 69,
 *           "name": "A4",
 *           "ticks": 0,
 *           "time": 0,
 *           "velocity": 0.6299212598425197
 */
public class MidiNote {
    @JsonProperty(value = "duration")
    double duration;
    @JsonProperty(value = "durationTicks")
    int durationTicks;
    @JsonProperty(value = "midi")
    int midi;
    @JsonProperty(value = "name")
    String name;
    @JsonProperty(value = "ticks")
    int ticks;
    @JsonProperty(value = "time")
    double time;
    @JsonProperty(value = "velocity")
    double velocity;
}
