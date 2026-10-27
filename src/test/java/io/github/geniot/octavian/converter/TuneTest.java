package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.verovio.json.Tune;
import io.github.geniot.octavian.converter.verovio.midi.MidiRoot;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TuneTest {
    static void main(String[] args) {
        try {
            String jsonStr = Files.readString(Path.of("tmp/out.json"), StandardCharsets.UTF_8);
            JsonMapper jsonMapper = new JsonMapper();
            MidiRoot midiRoot = jsonMapper.readValue(jsonStr, MidiRoot.class);

            Tune tune  = new Tune();
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tune);

            Files.writeString(Path.of("tmp/actual.json"), json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
