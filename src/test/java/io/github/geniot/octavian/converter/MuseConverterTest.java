package io.github.geniot.octavian.converter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Iterate over the tunes from the repository,
 * use the source file to generate player content,
 * compare the content with what we have in the database.
 *
 * It's like a compiler that checks its own validity.
 */
@Disabled
public class MuseConverterTest {
    @Test
    public void checksum() throws Exception {
        Properties properties = new Properties();
        properties.load(MuseConverterTest.class.getClassLoader().getResourceAsStream("application-test.properties"));

        try (Connection con = DriverManager
                .getConnection(properties.getProperty("spring.datasource.url"),
                        properties.getProperty("spring.datasource.username"),
                        properties.getProperty("spring.datasource.password"))) {

            String selectSql = "SELECT * FROM tune";
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
                System.out.println(resultSet.getString("title"));
            }

        } catch (Exception ex) {
            fail(ex);
        }
    }
}
