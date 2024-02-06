package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.geniot.octavian.converter.model.*;
import io.github.geniot.octavian.converter.tools.LayoutHandler;
import io.github.geniot.octavian.converter.tools.MuseConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static io.github.geniot.octavian.converter.tools.Utils.decompressBytes;
import static io.github.geniot.octavian.converter.tools.Utils.getMuseXml;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Iterate over the tunes from the repository,
 * use the source file to generate player content,
 * compare the content with what we have in the database.
 * <p>
 * It's like a compiler that checks its own validity.
 */
@Disabled
public class MuseConverterTest {
    @Test
    public void checksum() throws Exception {
        LayoutHandler layoutHandler = new LayoutHandler();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Properties properties = new Properties();
        properties.load(MuseConverterTest.class.getClassLoader().getResourceAsStream("application-test.properties"));
        MuseConverter museConverter = new MuseConverter();
        museConverter.setMuseScoreRun(properties.getProperty("musescore.run"));

        try (Connection con = DriverManager
                .getConnection(properties.getProperty("spring.datasource.url"),
                        properties.getProperty("spring.datasource.username"),
                        properties.getProperty("spring.datasource.password"))) {

            String selectSql = "SELECT * FROM tune WHERE id=53";//61,39,54,56,60,53,64,102,103,115,
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                byte[] museXmlCompressedBytes = resultSet.getBytes("mscz");
                String museXml = getMuseXml(museXmlCompressedBytes);
                int pngHeight = 300;

                int id = resultSet.getInt("id");
                String author = resultSet.getString("author");
                String title = resultSet.getString("title");
                Instrument instrument = Instrument.valueOf(resultSet.getString("instrument"));
                System.out.println(id + " " + author + " " + title);

                MuseConversionResponse response = museConverter.convert(museXml, pngHeight, author, title, instrument);
//                int[] repeats = objectMapper.readValue(decompressBytes(resultSet.getBytes("repeats")), int[].class);

                Tune playerTune = response.getTune();
                Tune playerExpectedTune = objectMapper.readValue(decompressBytes(resultSet.getBytes("json")), Tune.class);

                stripFingers(playerTune);
                stripFingers(playerExpectedTune);

                stripChanging(playerTune);
                stripChanging(playerExpectedTune);

//                Tune playerRepeatsTune = objectMapper.readValue(decompressBytes(resultSet.getBytes("json_repeats")), Tune.class);
//                layoutHandler.mergeFingering(playerTune, playerRepeatsTune, repeats);

                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerTune);
                String expectedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerExpectedTune);
                json = json.replaceAll("\r\n", "\n");
                expectedJson = expectedJson.replaceAll("\r\n", "\n");

                if (!expectedJson.equals(json)) {
                    System.out.println(id);
                }
                Assertions.assertEquals(expectedJson, json);
            }

        } catch (Exception ex) {
            fail(ex);
        }
    }

    private void stripChanging(Tune tune) {
        tune.setBarOffsets(null);
        tune.setSheetWidth(0);
        for (Point point : tune.getPoints()) {
            point.setOffsetX(0);
        }
    }

    public static void stripFingers(Tune tune) {
        for (Point point : tune.getPoints()) {
            if (point.getNotesOn() != null) {
                for (Note note : point.getNotesOn()) {
                    note.setFingers(null);
                }
            }
            if (point.getNotesOff() != null) {
                for (Note note : point.getNotesOff()) {
                    note.setFingers(null);
                }
            }
        }
    }
}
