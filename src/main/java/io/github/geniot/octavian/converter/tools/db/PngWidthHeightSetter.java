package io.github.geniot.octavian.converter.tools.db;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PngWidthHeightSetter {

    public static void main(String[] args) {
        try {
            try (Connection con = DriverManager
                    .getConnection("jdbc:postgresql://plumbum:5432/octavian", "octavian", "octavian")) {

                Statement selectStatement = con.createStatement();
                String selectSql = "SELECT id,png FROM tune;";
                ResultSet resultSet = selectStatement.executeQuery(selectSql);
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    byte[] png = resultSet.getBytes("png");

                    ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
                    ImageInputStream imageInputStream = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
                    reader.setInput(imageInputStream);
                    int width = reader.getWidth(reader.getMinIndex());
                    int height = reader.getHeight(reader.getMinIndex());

                    String updateSql = "UPDATE tune SET png_width='" + width + "',png_height='" + height + "' WHERE ID=" + id + ";";
                    Statement updateStatement = con.createStatement();
                    updateStatement.execute(updateSql);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
