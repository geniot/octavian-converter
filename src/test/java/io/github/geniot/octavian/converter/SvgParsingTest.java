package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.geniot.octavian.converter.verovio.PngHandler;
import io.github.geniot.octavian.converter.verovio.svg.SvgRoot;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;

public class SvgParsingTest {
    static void main(String[] args) throws Exception {
        String svgStr = Files.readString(Path.of("tmp/out_svg.xml"), StandardCharsets.UTF_8);
        byte[] pngBbs = Files.readAllBytes(Path.of("tmp/out.png"));

        PngHandler pngHandler = new PngHandler();
        Dimension dimension = pngHandler.getPngDimension(pngBbs);
        System.out.println("PNG Dimension: " + dimension);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SvgRoot svgRoot = xmlMapper.readValue(svgStr, SvgRoot.class);

        SortedSet<Integer> svgBarOffsets = svgRoot.definitionScale.pageMargin.svgSystem.geBarOffsets();
        svgBarOffsets.removeLast();
        Assertions.assertEquals(svgRoot.definitionScale.pageMargin.svgSystem.getMeasures().size(), svgBarOffsets.size());
        SortedSet<Integer> pngBarOffsets = getPngBarOffsets(svgRoot, svgBarOffsets, dimension);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBbs));
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        BasicStroke bs = new BasicStroke(1);
        g2d.setStroke(bs);
        for (Integer pngBarOffset : pngBarOffsets) {
            g2d.drawLine(pngBarOffset, 0, pngBarOffset, image.getHeight());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        IOUtils.write(baos.toByteArray(), new FileOutputStream("tmp/out_debug.png"));


        System.out.println(pngBarOffsets);
    }

    private static @NonNull SortedSet<Integer> getPngBarOffsets(SvgRoot svgRoot, SortedSet<Integer> svgBarOffsets, Dimension dimension) {
        Pair<Long, Long> translation = svgRoot.definitionScale.pageMargin.getTranslation();
        Rectangle viewBox = svgRoot.definitionScale.getViewBox();
        //22303 * 273 / 97720
        SortedSet<Integer> pngBarOffsets = new TreeSet<>();
        for (Integer svgBarOffset : svgBarOffsets) {
            long pngBarOffset = dimension.width * (svgBarOffset + translation.getLeft()) / viewBox.width;
            if (pngBarOffset < 0) {
                throw new RuntimeException("");
            }
            pngBarOffsets.add((int) pngBarOffset);
        }
        return pngBarOffsets;
    }
}
