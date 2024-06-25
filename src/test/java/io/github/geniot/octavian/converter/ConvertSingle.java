package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.model.Instrument;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import io.github.geniot.octavian.converter.tools.MuseConverter;
import org.apache.commons.io.IOUtils;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Converts a sample tune to provide
 */
public class ConvertSingle {
    public static void main(String[] args) {
        try {
            MuseConverter museConverter = new MuseConverter();
            museConverter.setMuseScoreRun("xvfb-run -a -e /dev/stdout /opt/musescore/MuseScore-3.6.2.548021370-x86_64.AppImage");
            StringWriter writer = new StringWriter();
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResource("the_green_grasshopper.mscx").openStream(), writer, StandardCharsets.UTF_8);
            MuseConversionResponse response = museConverter.convert(writer.toString(), 300, "test", "test", Instrument.PIANO, false);
            System.out.println(response.getTune().getTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
