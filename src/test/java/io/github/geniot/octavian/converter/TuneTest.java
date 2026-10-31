package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.geniot.octavian.converter.verovio.MusicXmlHandler;
import io.github.geniot.octavian.converter.verovio.PngHandler;
import io.github.geniot.octavian.converter.verovio.SvgHandler;
import io.github.geniot.octavian.converter.verovio.json.Point;
import io.github.geniot.octavian.converter.verovio.json.Tune;
import io.github.geniot.octavian.converter.verovio.midi.MidiRoot;
import io.github.geniot.octavian.converter.verovio.svg.SvgRoot;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;

public class TuneTest {
    static void main(String[] args) {
        try {
            Tune tune = new Tune();

            //title, author
            String musicXmlStr = Files.readString(Path.of("tmp/out.xml"), StandardCharsets.UTF_8);
            MusicXmlHandler handler = new MusicXmlHandler();
            String[] titleAndAuthor = handler.getTitleAndAuthor(musicXmlStr);
            tune.setTitle(titleAndAuthor[0]);
            tune.setAuthor(titleAndAuthor[1]);

            //png height, width
            PngHandler pngHandler = new PngHandler();
            Dimension dimension = pngHandler.getPngDimension(Files.readAllBytes(Path.of("tmp/out.png")));
            tune.setPngHeight(dimension.height);
            tune.setPngWidth(dimension.width);

            //selection ranges
            tune.setSelectionRanges(new int[]{});

            //bar offsets
            SvgHandler svgHandler = new SvgHandler();
            String svgStr = Files.readString(Path.of("tmp/out_svg.xml"), StandardCharsets.UTF_8);
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SvgRoot svgRoot = xmlMapper.readValue(svgStr, SvgRoot.class);

            SortedSet<Integer> barOffsets = svgHandler.getBarOffsets(svgRoot, dimension);
            tune.setBarOffsets(toInts(barOffsets));

            //sheet height, width
            Rectangle viewBox = svgRoot.definitionScale.getViewBox();
            float newWidth = 300f * (viewBox.width+260f) / (float) viewBox.height;
            tune.setSheetHeight(300);
            tune.setSheetWidth((int) newWidth);

            //play head width
            tune.setPlayHeadWidth(svgHandler.getPlayHeadWidth(svgRoot));

            //points
            String jsonStr = Files.readString(Path.of("tmp/out.json"), StandardCharsets.UTF_8);
            JsonMapper jsonMapper = new JsonMapper();
            MidiRoot midiRoot = jsonMapper.readValue(jsonStr, MidiRoot.class);
            tune.setPoints(new Point[]{});


            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tune);

            Files.writeString(Path.of("tmp/actual.json"), json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static int[] toInts(SortedSet<Integer> barOffsets) {
        int[] ints = new int[barOffsets.size()];
        int counter = 0;
        for (Integer i : barOffsets) {
            ints[counter++] = i;
        }
        return ints;
    }
}
