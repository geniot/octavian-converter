package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.midi.MidiRoot;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MidiParsingTest {
    static void main(String[] args) throws Exception {
        String jsonStr = Files.readString(Path.of("tmp/out.json"), StandardCharsets.UTF_8);
        JsonMapper jsonMapper = new JsonMapper();
        MidiRoot midiRoot = jsonMapper.readValue(jsonStr, MidiRoot.class);
        System.out.println(midiRoot);
    }
}
