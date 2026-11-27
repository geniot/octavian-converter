package io.github.geniot.octavian.converter;

import org.apache.commons.io.IOUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConvertTest {
    private static final JsonMapper jsonMapper = new JsonMapper();
    private static final String basePath = "/home/vitaly/WebstormProjects/octavian/docs/";

    static void main(String[] args) {
        try {
            System.out.println(Constants.NOTES.size());//to skip chords debugging
            String selected = "rondo_alla_turca";
            String instrument = "piano";
            String menuJson = IOUtils.toString(new FileInputStream(basePath + instrument + "/menu.json"), StandardCharsets.UTF_8);
            MenuRoot menuRoot = jsonMapper.readValue(menuJson, MenuRoot.class);
            for (MenuItem menuItem : menuRoot.getMenuItems()) {
                if (selected != null && menuItem.getPath().endsWith(selected)) {
                    System.out.println(menuItem.getPath());
                    convert(menuItem);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void convert(MenuItem menuItem) throws Exception {
        String tuneTag = menuItem.getPath().substring(menuItem.getPath().lastIndexOf('/') + 1);
        String musicXmlPath = basePath + menuItem.getPath() + '/' + tuneTag + ".musicxml";
        OctavianConverter museConverter = new OctavianConverter();
        String xml = IOUtils.toString(new FileInputStream(musicXmlPath), StandardCharsets.UTF_8);
        ConversionResponse response = museConverter.convert(xml);

        List<byte[]> splits = splitPng(response.tune.getPngWidth(), response.tune.getPngHeight(), response.pngSheet);
        for (int i = 0; i < splits.size(); i++) {
            IOUtils.write(splits.get(i), new FileOutputStream(basePath + menuItem.getPath() + '/' + tuneTag + "_" + i + ".png"));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.tune);
        Files.writeString(Path.of(basePath + menuItem.getPath() + '/' + tuneTag + ".json"), json);
    }

    private static List<byte[]> splitPng(int pngWidth, int pngHeight, byte[] png) throws Exception {
        List<byte[]> splits = new ArrayList<>();
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(png));
        int count = pngWidth / 30000;
        int rest = pngWidth % 30000;
        for (int i = 0; i < count; i++) {
            BufferedImage subImage = bufferedImage.getSubimage(i * 30000, 0, 30000, pngHeight);
            splits.add(PngHandler.image2bytes(subImage));
        }
        if (rest > 0) {
            BufferedImage subImage = bufferedImage.getSubimage(count * 30000, 0, rest, pngHeight);
            splits.add(PngHandler.image2bytes(subImage));
        }
        return splits;
    }
}
