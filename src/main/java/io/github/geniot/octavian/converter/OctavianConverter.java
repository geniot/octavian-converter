package io.github.geniot.octavian.converter;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.json.Point;
import io.github.geniot.octavian.converter.json.Tune;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.SortedSet;


/**
 * Produces files necessary for the Octavian player.
 * Input: MusicXML or MuseScore (.mscz, .mscx)
 * Output: MP3, PNG, json
 */
@Data
@Component
public class OctavianConverter {
    static Logger logger = LoggerFactory.getLogger(OctavianConverter.class);

    ProcessStarter processStarter = new ProcessStarter();
    PointsHandler pointsHandler = new PointsHandler();
    JsonMapper jsonMapper = new JsonMapper();

    public ConversionResponse convert(String xml) throws Exception {
        ConversionResponse response = new ConversionResponse();

        MusicXmlHandler musicXmlHandler = new MusicXmlHandler(xml);
//        FileUtils.write(new File("tmp/out.xml"), musicXmlHandler.document2string(), StandardCharsets.UTF_8);

        String mei = processStarter.xml2mei(musicXmlHandler.document2string());
        MeiHandler meiHandler = new MeiHandler(mei);

        String svg = processStarter.mei2svg(meiHandler.document2string());
        SvgHandler svgHandler = new SvgHandler(svg);

        byte[] png = processStarter.getPngBytes(svgHandler.document2string());
        PngHandler pngHandler = new PngHandler(png);

        byte[] midi = processStarter.xml2midi(musicXmlHandler.document2string());
        String midiJson = processStarter.midi2json(midi);
        MidiRoot midiRoot = jsonMapper.readValue(midiJson, MidiRoot.class);
        meiHandler.setIdsToMidiNotes(midiRoot);

        Tune tune = new Tune();
        String[] titleAndAuthor = musicXmlHandler.getTitleAndAuthor();
        tune.setTitle(titleAndAuthor[0]);
        tune.setAuthor(titleAndAuthor[1]);
        tune.setPngHeight(pngHandler.image.getHeight());
        tune.setPngWidth(pngHandler.image.getWidth());
        tune.setSheetHeight(pngHandler.image.getHeight());
        tune.setSheetWidth(pngHandler.image.getWidth());
        tune.setBarOffsets(toInts(svgHandler.getBarOffsets()));
        tune.setPlayHeadWidth(svgHandler.getPlayHeadWidth() + 1);

        IndexedTreeMap<Integer, Point> points = new IndexedTreeMap<>();
        pointsHandler.addPoints(points, midiRoot, svgHandler.geNoteOffsets());
        pointsHandler.setOffsetsToOffPoints(new ArrayList<>(points.values()), tune.getSheetWidth());
        pointsHandler.setBars(points, tune.getBarOffsets());
        tune.setPoints(points.sequencedValues().toArray(new Point[0]));

        response.setTune(tune);
        response.setPngSheet(pngHandler.image2bytes());

        return response;
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
