package io.github.geniot.octavian.converter;

import org.apache.commons.io.IOUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConvertTest {
    static void main(String[] args) {
        try {
            String path = "/home/vitaly/IdeaProjects/octavian-data/piano/greensleeves/greensleeves.musicxml";
//            String path = "/home/vitaly/IdeaProjects/octavian-data/piano/autumn_leaves/autumn_leaves.musicxml";
            OctavianConverter museConverter = new OctavianConverter();
            String xml = IOUtils.toString(new FileInputStream(path), StandardCharsets.UTF_8);
            ConversionResponse response = museConverter.convert(xml);

            IOUtils.write(response.pngSheet, new FileOutputStream("tmp/out.png"));
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.tune);
            Files.writeString(Path.of("tmp/actual.json"), json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
