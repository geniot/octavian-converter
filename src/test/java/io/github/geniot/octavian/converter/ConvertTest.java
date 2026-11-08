package io.github.geniot.octavian.converter;

import org.apache.commons.io.IOUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConvertTest {
    private static final JsonMapper jsonMapper = new JsonMapper();
    private static final String basePath = "/home/vitaly/WebstormProjects/octavian/public/";

    static void main(String[] args) {
        try {
            String menuJson = IOUtils.toString(new FileInputStream(basePath + "menu.json"), StandardCharsets.UTF_8);
            MenuRoot menuRoot = jsonMapper.readValue(menuJson, MenuRoot.class);
            for (MenuItem menuItem : menuRoot.getMenuItems()) {
                if (menuItem.getInstrument().equals("piano")) {
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

        IOUtils.write(response.pngSheet, new FileOutputStream(basePath + menuItem.getPath() + '/' + tuneTag + "_0.png"));
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.tune);
        Files.writeString(Path.of(basePath + menuItem.getPath() + '/' + tuneTag + ".json"), json);
    }
}
