package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.geniot.octavian.converter.verovio.svg.SvgRoot;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;

public class SvgParsingTest {
    static void main(String[] args) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String svgStr = Files.readString(Path.of("tmp/out_svg.xml"), StandardCharsets.UTF_8);
        SvgRoot svgRoot = xmlMapper.readValue(svgStr, SvgRoot.class);
        SortedSet<Integer> barOffsets = svgRoot.definitionScale.pageMargin.svgSystem.geBarOffsets();
        //22303 * 273 / 97720


        System.out.println(barOffsets);
    }
}
