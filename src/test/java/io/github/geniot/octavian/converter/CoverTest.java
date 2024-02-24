package io.github.geniot.octavian.converter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.Base64;
import java.util.Properties;

public class CoverTest {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(MuseConverterTest.class.getClassLoader().getResourceAsStream("application-test.properties"));

        try (Connection con = DriverManager
                .getConnection(properties.getProperty("spring.datasource.url"),
                        properties.getProperty("spring.datasource.username"),
                        properties.getProperty("spring.datasource.password"))) {

            String selectSql = "SELECT id,cover FROM tune";
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                System.out.println(id);
                byte[] fileContent = resultSet.getBytes("cover");
                if (fileContent == null) {
                    continue;
                }

                BufferedImage img = ImageIO.read(new ByteArrayInputStream(fileContent));
                BufferedImage newBufferedImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_BGR);
                newBufferedImage.createGraphics().drawImage(img, 0, 0, Color.white, null);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(newBufferedImage, "jpg", byteArrayOutputStream);

//                FileOutputStream fileOutputStream1 = new FileOutputStream("1.png");
//                fileOutputStream1.write(fileContent);
//                fileOutputStream1.close();
//
//                FileOutputStream fileOutputStream2 = new FileOutputStream("1.jpg");
//                fileOutputStream2.write(byteArrayOutputStream.toByteArray());
//                fileOutputStream2.close();

                String encodedStringPng = Base64.getEncoder().encodeToString(fileContent);
                String encodedStringJpg = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

                System.out.println(encodedStringPng.length() + ":" + encodedStringJpg.length());

                String updateSql = "UPDATE tune SET cover_txt=? WHERE ID=?";
                PreparedStatement statement = con.prepareStatement(updateSql);
                statement.setString(1, "data:image/jpeg;base64," + encodedStringJpg);
                statement.setInt(2, id);
                statement.executeUpdate();
            }
        }
    }
}
