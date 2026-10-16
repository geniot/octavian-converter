package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.model.Instrument;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import io.github.geniot.octavian.converter.tools.VerovioConverter;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class ConvertVerovio {
    public static void main(String[] args) {
        try {
            String path = "/home/vitaly/IdeaProjects/octavian-data/piano/arabesque_etude_no_2_op_100/arabesque_etude_no_2_op_100.xml";
            VerovioConverter museConverter = new VerovioConverter();
            String xml = IOUtils.toString(new FileInputStream(path), StandardCharsets.UTF_8);
            MuseConversionResponse response = museConverter.convert(xml, 300, "test", "test", Instrument.ACCORDION);
            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
