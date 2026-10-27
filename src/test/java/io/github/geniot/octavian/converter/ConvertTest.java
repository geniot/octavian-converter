package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.verovio.MuseConversionResponse;
import io.github.geniot.octavian.converter.verovio.OctavianConverter;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class ConvertTest {
    static void main(String[] args) {
        try {
            String path = "/home/vitaly/IdeaProjects/octavian-data/piano/greensleeves/greensleeves.musicxml";
            OctavianConverter museConverter = new OctavianConverter();
            String xml = IOUtils.toString(new FileInputStream(path), StandardCharsets.UTF_8);
            MuseConversionResponse response = museConverter.convert(xml);
            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
