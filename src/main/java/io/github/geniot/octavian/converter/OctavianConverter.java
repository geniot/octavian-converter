package io.github.geniot.octavian.converter;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.json.Point;
import io.github.geniot.octavian.converter.json.Tune;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Produces files necessary for the Octavian player.
 * Input: MusicXML or MuseScore (.mscz, .mscx)
 * Output: MP3, PNG, json
 */
@Slf4j
@Data
@Component
public class OctavianConverter {

    ProcessStarter processStarter = new ProcessStarter();
    PointsHandler pointsHandler = new PointsHandler();
    JsonMapper jsonMapper = new JsonMapper();

    public ConversionResponse convert(String xml) throws Exception {
        ConversionResponse response = new ConversionResponse();

        MusicXmlHandler musicXmlHandler = new MusicXmlHandler(xml);
        FileUtils.write(new File("tmp/out.xml"), musicXmlHandler.document2string(), StandardCharsets.UTF_8);

        String mei = processStarter.xml2mei(musicXmlHandler.document2string());
        MeiHandler meiHandler = new MeiHandler(mei, true);
        FileUtils.write(new File("tmp/out.mei"), meiHandler.document2string(), StandardCharsets.UTF_8);

        String svg = processStarter.mei2svg(meiHandler.document2string());
        SvgHandler svgHandler = new SvgHandler(svg);
        FileUtils.write(new File("tmp/out.svg"), svgHandler.document2string(), StandardCharsets.UTF_8);
        Map<String, Integer> noteOffsets = svgHandler.geNoteOffsets();

        byte[] png1 = processStarter.getPngBytesJairoSVG(svgHandler.document2string());
//        byte[] png2 = processStarter.getPngBytesEchoSvg(svgHandler.document2string());
//        byte[] png3 = processStarter.getPngBytesJSVG(svgHandler.document2string());
        PngHandler pngHandler = new PngHandler(png1);
        FileUtils.writeByteArrayToFile(new File("tmp/out.png"), pngHandler.image2bytes());

        PngHandler pngDebugHandler = new PngHandler(png1);
        pngDebugHandler.debugLines(new TreeSet<>(noteOffsets.values()), Color.BLUE, new BasicStroke(1));
        FileUtils.writeByteArrayToFile(new File("tmp/out_debug.png"), pngDebugHandler.image2bytes());

        byte[] midi = processStarter.xml2midi(musicXmlHandler.document2string());
        FileUtils.writeByteArrayToFile(new File("tmp/out.mid"), midi);
        String midiJson = processStarter.midi2json(midi);
        FileUtils.write(new File("tmp/out.json"), midiJson, StandardCharsets.UTF_8);
        MidiRoot midiRoot = jsonMapper.readValue(midiJson, MidiRoot.class);
        meiHandler.setIdsToMidiNotes(midiRoot, invert(noteOffsets, String.valueOf(1)), invert(noteOffsets, String.valueOf(2)));

        Tune tune = new Tune();
        String[] titleAndAuthor = musicXmlHandler.getTitleAndAuthor();
        tune.setTitle(titleAndAuthor[0]);
        tune.setAuthor(titleAndAuthor[1]);
        tune.setPngHeight(pngHandler.image.getHeight());
        tune.setPngWidth(pngHandler.image.getWidth());
        tune.setSheetHeight(pngHandler.image.getHeight());
        tune.setSheetWidth(pngHandler.image.getWidth());
        tune.setBarOffsets(toInts(svgHandler.getBarOffsets()));
        tune.setSelectionRanges(toInts(svgHandler.getSelectionRanges()));
        tune.setPlayHeadWidth(svgHandler.getPlayHeadWidth() + 1);

        IndexedTreeMap<Integer, Point> points = new IndexedTreeMap<>();
        pointsHandler.addPoints(points, midiRoot, noteOffsets);
        pointsHandler.setOffsetsToOffPoints(new ArrayList<>(points.values()), tune.getSheetWidth());
        pointsHandler.setBars(points, tune.getBarOffsets());
        if (musicXmlHandler.getInstrument().equals(Instrument.Accordion)) {
            pointsHandler.convertBassNotesToChords(points);
        }
        points = pointsHandler.splitOnOffNotes(points);
        tune.setPoints(points.sequencedValues().toArray(new Point[0]));

        MeiHandler meiOriginalHandler = new MeiHandler(mei, false);
        meiOriginalHandler.setFingeringToTune(tune);

        response.setTune(tune);
        response.setPngSheet(pngHandler.image2bytes());

        return response;
    }

    private SortedMap<Integer, Set<String>> invert(Map<String, Integer> noteOffsets, String part) {
        SortedMap<Integer, Set<String>> result = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : noteOffsets.entrySet()) {
            String key = entry.getKey();
            String[] splits = key.split("-");
            String keyPart = splits[splits.length - 1];
            if (splits[splits.length - 1].startsWith("rend")) {
                keyPart = splits[splits.length - 2];
            }
            if (keyPart.equals(part)) {
                Set<String> ids = result.getOrDefault(entry.getValue(), new HashSet<>());
                ids.add(entry.getKey());
                result.put(entry.getValue(), ids);
            }
        }
        return result;
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
