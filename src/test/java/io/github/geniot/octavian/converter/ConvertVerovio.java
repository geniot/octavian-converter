package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.model.Instrument;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import io.github.geniot.octavian.converter.tools.VerovioConverter;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

public class ConvertVerovio {
    public static void main(String[] args) {
        try {
            VerovioConverter museConverter = new VerovioConverter();
            String xml = IOUtils.resourceToString("your_honour_lady_luck.xml", StandardCharsets.UTF_8, ConvertVerovio.class.getClassLoader());
            MuseConversionResponse response = museConverter.convert(xml, 300, "test", "test", Instrument.ACCORDION);
            System.out.println(response.getTune().getTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
