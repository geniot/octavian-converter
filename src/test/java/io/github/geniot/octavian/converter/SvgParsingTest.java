package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.geniot.octavian.converter.verovio.PngHandler;
import io.github.geniot.octavian.converter.verovio.SvgHandler;
import io.github.geniot.octavian.converter.verovio.svg.SvgRoot;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;

public class SvgParsingTest {
    static void main(String[] args) throws Exception {
        String svgStr = Files.readString(Path.of("tmp/out_svg.xml"), StandardCharsets.UTF_8);
        byte[] pngBbs = Files.readAllBytes(Path.of("tmp/out.png"));

        PngHandler pngHandler = new PngHandler();
        SvgHandler svgHandler = new SvgHandler();

        Dimension pngDimension = pngHandler.getPngDimension(pngBbs);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SvgRoot svgRoot = xmlMapper.readValue(svgStr, SvgRoot.class);

        SortedSet<Integer> barOffsets = svgHandler.getBarOffsets(svgRoot, pngDimension);
        SortedSet<Integer> noteOffsets = svgHandler.getNoteOffsets(svgRoot, pngDimension);

        pngBbs = pngHandler.debugLines(pngBbs, barOffsets, Color.RED, new BasicStroke(1));
        pngBbs = pngHandler.debugLines(pngBbs, noteOffsets, Color.BLUE, new BasicStroke(1));
        IOUtils.write(pngBbs, new FileOutputStream("tmp/out_debug.png"));


        System.out.println(barOffsets);
    }


}
