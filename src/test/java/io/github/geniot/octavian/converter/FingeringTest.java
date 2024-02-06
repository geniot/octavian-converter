package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.geniot.octavian.converter.model.Tune;
import io.github.geniot.octavian.converter.tools.MuseConverter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

import static io.github.geniot.octavian.converter.tools.Utils.compressBytes;
import static io.github.geniot.octavian.converter.tools.Utils.decompressBytes;

public class FingeringTest {
    /**
     * Removes fingers from tunes
     *
     * @throws Exception
     */
    @Test
    public void strip() throws Exception {
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

            String selectSql = "SELECT id,title,json,json_repeats FROM tune";
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                System.out.println(id + " " + resultSet.getString("title"));

                Tune tune = objectMapper.readValue(decompressBytes(resultSet.getBytes("json")), Tune.class);
                Tune repeatsTune = objectMapper.readValue(decompressBytes(resultSet.getBytes("json")), Tune.class);
                boolean shouldUpdateRepeats = false;
                byte[] jsonRepeatsBbs = resultSet.getBytes("json_repeats");
                if (jsonRepeatsBbs != null && jsonRepeatsBbs.length > 0) {
                    repeatsTune = objectMapper.readValue(decompressBytes(resultSet.getBytes("json_repeats")), Tune.class);
                    shouldUpdateRepeats = true;
                }

                MuseConverterTest.stripFingers(tune);
                MuseConverterTest.stripFingers(repeatsTune);

                String tuneJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tune);
                String repeatsTuneJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(repeatsTune);

                byte[] tuneBbs = compressBytes(tuneJson.getBytes(StandardCharsets.UTF_8));
                byte[] tuneRepeatsBbs = compressBytes(repeatsTuneJson.getBytes(StandardCharsets.UTF_8));

                String updateSql = "UPDATE tune SET json=?,json_repeats=? WHERE ID=?";
                PreparedStatement statement = con.prepareStatement(updateSql);
                statement.setBytes(1, tuneBbs);
                statement.setBytes(2, shouldUpdateRepeats ? tuneRepeatsBbs : new byte[]{});
                statement.setInt(3, id);
                statement.executeUpdate();
            }
        }
    }
}
