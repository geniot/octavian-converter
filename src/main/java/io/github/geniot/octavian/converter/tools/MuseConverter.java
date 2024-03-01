package io.github.geniot.octavian.converter.tools;


import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.indexedtreemap.IndexedTreeSet;
import io.github.geniot.octavian.converter.model.*;
import lombok.Data;
import org.apache.batik.ext.awt.image.rendered.TileCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Generates PNG and JSON from MSCZ using MuseScore and Batik.
 * <p>
 * 1. generate 3 versions of musescore panorama: full, leftHand, rightHand
 * 2. generate midi files from leftHand, rightHand
 * 3. generate svg file from full panorama
 * 4. extract necessary data from midi and svg, generate json
 * 5. generate png from svg
 */
@Data
@Component
public class MuseConverter {
    static Logger logger = LoggerFactory.getLogger(MuseConverter.class);

    @Value("${musescore.run}")
    String museScoreRun;

    MuseHandler museHandler = new MuseHandler();
    SvgHandler svgHandler = new SvgHandler();
    MidiHandler midiHandler = new MidiHandler();
    PointsHandler pointsHandler = new PointsHandler();
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    public MuseConversionResponse convert(String museXml,
                                          int pngHeight,
                                          String author,
                                          String title,
                                          Instrument instrument,
                                          boolean shouldRemoveTmp) throws Exception {

        MuseConversionResponse MuseConversionResponse = new MuseConversionResponse();
        File tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory(String.valueOf(System.currentTimeMillis())).toFile();
            logger.info(tmpDir.getAbsolutePath());

            MuseFixResult museFixResult = museHandler.fixMuseXml(museXml);
            MuseHalfConversionResult museHalfConversionResult = getMuseHalfConversionResult(museFixResult, tmpDir);

            SvgData svgData = svgHandler.getSvgData(museHalfConversionResult.getSvgBytes(), museHalfConversionResult.getWideSvgBytes());
            SvgData repeatsSvgData = svgHandler.getSvgData(museHalfConversionResult.getRepeatsSvgBytes(), museHalfConversionResult.getRepeatsWideSvgBytes());

            float scaleFactor = svgData.getHeight() / pngHeight;
            float newWidth = svgData.getWidth() / scaleFactor;

            float repeatsScaleFactor = repeatsSvgData.getHeight() / pngHeight;
            float repeatsNewWidth = repeatsSvgData.getWidth() / repeatsScaleFactor;

            int[] barOffsets = rationalizeBarOffsets(svgData.getBarOffsets(), scaleFactor, 1);
            int[] repeatsBarOffsets = rationalizeBarOffsets(repeatsSvgData.getBarOffsets(), repeatsScaleFactor, 1);

            TreeMap<Long, Float> tempoMap = midiHandler.getTempoMap(new ByteArrayInputStream(museHalfConversionResult.getRightMidiBytes()));
            TreeMap<Long, Float> repeatsTempoMap = midiHandler.getTempoMap(new ByteArrayInputStream(museHalfConversionResult.getRepeatsRightMidiBytes()));

            midiHandler.setTicksToNotes(museFixResult.getRightRoot(), new ByteArrayInputStream(museHalfConversionResult.getRightMidiBytes()));
            midiHandler.setTicksToNotes(museFixResult.getLeftRoot(), new ByteArrayInputStream(museHalfConversionResult.getLeftMidiBytes()));
            midiHandler.setTicksToNotes(museFixResult.getRepRightRoot(), new ByteArrayInputStream(museHalfConversionResult.getRepeatsRightMidiBytes()));
            midiHandler.setTicksToNotes(museFixResult.getRepLeftRoot(), new ByteArrayInputStream(museHalfConversionResult.getRepeatsLeftMidiBytes()));

            museFixResult.getRightRoot().validateTicks();
            museFixResult.getLeftRoot().validateTicks();
            museFixResult.getRepRightRoot().validateTicks();
            museFixResult.getRepLeftRoot().validateTicks();

            museFixResult.getRightRoot().setTicksToSilentNotes();
            museFixResult.getLeftRoot().setTicksToSilentNotes();
            museFixResult.getRepRightRoot().setTicksToSilentNotes();
            museFixResult.getRepLeftRoot().setTicksToSilentNotes();

            museFixResult.getRightRoot().setSvgNotes(svgData.getRightHandNotes());
            museFixResult.getLeftRoot().setSvgNotes(svgData.getLeftHandNotes());
            museFixResult.getRepRightRoot().setSvgNotes(repeatsSvgData.getRightHandNotes());
            museFixResult.getRepLeftRoot().setSvgNotes(repeatsSvgData.getLeftHandNotes());

            String rightHandLayout = instrument.equals(Instrument.PIANO) ? Layout.PIANO_RIGHT_HAND : null;
            String leftHandLayout = instrument.equals(Instrument.PIANO) ? Layout.PIANO_LEFT_HAND : null;

            IndexedTreeMap<Integer, Point> rightPointsMap = museFixResult.getRightRoot().getPoints(scaleFactor, NoteType.RIGHT_HAND_NOTE, rightHandLayout);
            IndexedTreeMap<Integer, Point> leftPointsMap = museFixResult.getLeftRoot().getPoints(scaleFactor, NoteType.LEFT_HAND_BASS, leftHandLayout);
            IndexedTreeMap<Integer, Point> repeatsRightPointsMap = museFixResult.getRepRightRoot().getPoints(repeatsScaleFactor, NoteType.RIGHT_HAND_NOTE, rightHandLayout);
            IndexedTreeMap<Integer, Point> repeatsLeftPointsMap = museFixResult.getRepLeftRoot().getPoints(repeatsScaleFactor, NoteType.LEFT_HAND_BASS, leftHandLayout);

            pointsHandler.setBars(rightPointsMap, barOffsets);
            pointsHandler.setBars(leftPointsMap, barOffsets);
            pointsHandler.setBars(repeatsRightPointsMap, repeatsBarOffsets);
            pointsHandler.setBars(repeatsLeftPointsMap, repeatsBarOffsets);

            if (instrument == Instrument.ACCORDION) {
                pointsHandler.bassesToChords(leftPointsMap);
                pointsHandler.bassesToChords(repeatsLeftPointsMap);
            }

            IndexedTreeMap<Integer, Point> pointsMap = PointsHandler.mergePointMaps(rightPointsMap, leftPointsMap);
            pointsHandler.splitOnOff(pointsMap);
            pointsHandler.validateOffsets(pointsMap);
            pointsHandler.changeTicksToMilliseconds(pointsMap, tempoMap);
            pointsHandler.setOffsetsToOffPoints(pointsMap.values().toArray(new Point[0]), newWidth);
            pointsHandler.setBars(pointsMap, barOffsets);

            IndexedTreeMap<Integer, Point> repeatsPointsMap = PointsHandler.mergePointMaps(repeatsRightPointsMap, repeatsLeftPointsMap);
            pointsHandler.splitOnOff(repeatsPointsMap);
            pointsHandler.validateOffsets(repeatsPointsMap);
            pointsHandler.changeTicksToMilliseconds(repeatsPointsMap, repeatsTempoMap);
            pointsHandler.setOffsetsToOffPoints(repeatsPointsMap.values().toArray(new Point[0]), repeatsNewWidth);
            pointsHandler.setBars(repeatsPointsMap, repeatsBarOffsets);

            byte[] pngBytes = svgHandler.svg2png(museHalfConversionResult.getSvgBytes(), newWidth, pngHeight);
            byte[] repeatsBytes = svgHandler.svg2png(museHalfConversionResult.getRepeatsSvgBytes(), repeatsNewWidth, pngHeight);

            Tune tune = initTune(
                    Math.round(newWidth),
                    pngHeight,
                    author,
                    title,
                    (int) (svgData.getPlayHeadWidth() / scaleFactor),
                    barOffsets,
                    pointsMap.values().toArray(new Point[0])
            );

            Tune repeatsTune = initTune(
                    Math.round(repeatsNewWidth),
                    pngHeight,
                    author,
                    title,
                    (int) (repeatsSvgData.getPlayHeadWidth() / repeatsScaleFactor),
                    repeatsBarOffsets,
                    repeatsPointsMap.values().toArray(new Point[0])
            );

            MuseConversionResponse.setPngSheet(pngBytes);
            MuseConversionResponse.setRepeatsPngSheet(repeatsBytes);

            MuseConversionResponse.setTune(tune);
            MuseConversionResponse.setRepeatsTune(repeatsTune);

            MuseConversionResponse.setRepeats(museFixResult.getRepeats());

            return MuseConversionResponse;

        } finally {
            if (shouldRemoveTmp && tmpDir != null) {
                FileUtils.deleteDirectory(tmpDir);
            }
            try {
                TileCache.setSize(0);
                TileCache.setSize(2);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private Tune initTune(int width,
                          int height,
                          String author,
                          String title,
                          int playHeadWidth,
                          int[] barOffsets,
                          Point[] points) {
        Tune tune = new Tune();
        tune.setSheetWidth(width);
        tune.setSheetHeight(height);
        tune.setAuthor(author);
        tune.setTitle(title);
        tune.setPlayHeadWidth(playHeadWidth);
        tune.setBarOffsets(barOffsets);
        tune.setPoints(points);
        return tune;
    }

    private MuseHalfConversionResult getMuseHalfConversionResult(MuseFixResult museFixResult, File tmpDir) throws Exception {

        File tmpInputFile = new File(tmpDir, "score.mscx");
        File tmpWideInputFile = new File(tmpDir, "score_wide.mscx");
        File tmpLeftInputFile = new File(tmpDir, "score_left.mscx");
        File tmpRightInputFile = new File(tmpDir, "score_right.mscx");

        File tmpRepeatsInputFile = new File(tmpDir, "rep_score.mscx");
        File tmpRepeatsWideInputFile = new File(tmpDir, "rep_score_wide.mscx");
        File tmpRepeatsLeftInputFile = new File(tmpDir, "rep_score_left.mscx");
        File tmpRepeatsRightInputFile = new File(tmpDir, "rep_score_right.mscx");

        FileUtils.writeByteArrayToFile(tmpInputFile, museFixResult.getScore().getBytes(StandardCharsets.UTF_8));
        FileUtils.writeByteArrayToFile(tmpWideInputFile, museFixResult.getWideScore().getBytes(StandardCharsets.UTF_8));
        FileUtils.writeByteArrayToFile(tmpLeftInputFile, museFixResult.getLeftScore().getBytes(StandardCharsets.UTF_8));
        FileUtils.writeByteArrayToFile(tmpRightInputFile, museFixResult.getRightScore().getBytes(StandardCharsets.UTF_8));

        FileUtils.writeByteArrayToFile(tmpRepeatsInputFile, museFixResult.getRepScore().getBytes(StandardCharsets.UTF_8));
        FileUtils.writeByteArrayToFile(tmpRepeatsWideInputFile, museFixResult.getRepWideScore().getBytes(StandardCharsets.UTF_8));
        FileUtils.writeByteArrayToFile(tmpRepeatsLeftInputFile, museFixResult.getRepLeftScore().getBytes(StandardCharsets.UTF_8));
        FileUtils.writeByteArrayToFile(tmpRepeatsRightInputFile, museFixResult.getRepRightScore().getBytes(StandardCharsets.UTF_8));

        //MuseScore adds page number to the file's base name, we expect only one page
        String svgOutputPath1 = tmpDir.getAbsolutePath() + File.separator + "score.svg";
        String svgOutputPath2 = tmpDir.getAbsolutePath() + File.separator + "score-1.svg";
        String wideSvgOutputPath1 = tmpDir.getAbsolutePath() + File.separator + "score_wide.svg";
        String wideSvgOutputPath2 = tmpDir.getAbsolutePath() + File.separator + "score_wide-1.svg";
        String midiLeftOutput = tmpDir.getAbsolutePath() + File.separator + "score_left.mid";
        String midiRightOutput = tmpDir.getAbsolutePath() + File.separator + "score_right.mid";

        //for the fingering editor
        String repeatsSvgOutputPath1 = tmpDir.getAbsolutePath() + File.separator + "rep_score.svg";
        String repeatsSvgOutputPath2 = tmpDir.getAbsolutePath() + File.separator + "rep_score-1.svg";
        String repeatsWideSvgOutputPath1 = tmpDir.getAbsolutePath() + File.separator + "rep_score_wide.svg";
        String repeatsWideSvgOutputPath2 = tmpDir.getAbsolutePath() + File.separator + "rep_score_wide-1.svg";
        String repeatsMidiLeftOutput = tmpDir.getAbsolutePath() + File.separator + "rep_score_left.mid";
        String repeatsMidiRightOutput = tmpDir.getAbsolutePath() + File.separator + "rep_score_right.mid";

        HashSet<Callable<Void>> callables = new HashSet<>();
        callables.add(() -> runProcess(museScoreRun + " --trim-image 0 -o " + svgOutputPath1 + " " + tmpInputFile.getAbsolutePath()));
        callables.add(() -> runProcess(museScoreRun + " --trim-image 0 -o " + wideSvgOutputPath1 + " " + tmpWideInputFile.getAbsolutePath()));
        callables.add(() -> runProcess(museScoreRun + " --dump-midi-out -o " + midiLeftOutput + " " + tmpLeftInputFile.getAbsolutePath()));
        callables.add(() -> runProcess(museScoreRun + " --dump-midi-out -o " + midiRightOutput + " " + tmpRightInputFile.getAbsolutePath()));

        callables.add(() -> runProcess(museScoreRun + " --trim-image 0 -o " + repeatsSvgOutputPath1 + " " + tmpRepeatsInputFile.getAbsolutePath()));
        callables.add(() -> runProcess(museScoreRun + " --trim-image 0 -o " + repeatsWideSvgOutputPath1 + " " + tmpRepeatsWideInputFile.getAbsolutePath()));
        callables.add(() -> runProcess(museScoreRun + " --dump-midi-out -o " + repeatsMidiLeftOutput + " " + tmpRepeatsLeftInputFile.getAbsolutePath()));
        callables.add(() -> runProcess(museScoreRun + " --dump-midi-out -o " + repeatsMidiRightOutput + " " + tmpRepeatsRightInputFile.getAbsolutePath()));
        executorService.invokeAll(callables);

        byte[] svgBytes = FileUtils.readFileToByteArray(new File(svgOutputPath2));
        byte[] wideSvgBytes = FileUtils.readFileToByteArray(new File(wideSvgOutputPath2));
        byte[] leftMidiBytes = FileUtils.readFileToByteArray(new File(midiLeftOutput));
        byte[] rightMidiBytes = FileUtils.readFileToByteArray(new File(midiRightOutput));

        byte[] repeatsSvgBytes = FileUtils.readFileToByteArray(new File(repeatsSvgOutputPath2));
        byte[] repeatsWideSvgBytes = FileUtils.readFileToByteArray(new File(repeatsWideSvgOutputPath2));
        byte[] repeatsLeftMidiBytes = FileUtils.readFileToByteArray(new File(repeatsMidiLeftOutput));
        byte[] repeatsRightMidiBytes = FileUtils.readFileToByteArray(new File(repeatsMidiRightOutput));

        MuseHalfConversionResult museHalfConversionResult = new MuseHalfConversionResult();

        museHalfConversionResult.setSvgBytes(svgBytes);
        museHalfConversionResult.setWideSvgBytes(wideSvgBytes);
        museHalfConversionResult.setLeftMidiBytes(leftMidiBytes);
        museHalfConversionResult.setRightMidiBytes(rightMidiBytes);

        museHalfConversionResult.setRepeatsSvgBytes(repeatsSvgBytes);
        museHalfConversionResult.setRepeatsWideSvgBytes(repeatsWideSvgBytes);
        museHalfConversionResult.setRepeatsLeftMidiBytes(repeatsLeftMidiBytes);
        museHalfConversionResult.setRepeatsRightMidiBytes(repeatsRightMidiBytes);

        return museHalfConversionResult;
    }

    private int[] rationalizeBarOffsets(IndexedTreeSet<Float> barOffsets, float ratio, int truncate) {
        int[] ints = new int[barOffsets.size() - truncate];
        for (int i = 0; i < barOffsets.size() - truncate; i++) {
            ints[i] = (int) (barOffsets.exact(i) / ratio);
        }
        return ints;
    }

    private Void runProcess(String commandLine) {
        try {
            logger.info(commandLine);
            Process process = Runtime.getRuntime().exec(commandLine);
            outPut(process.getInputStream(), Level.INFO);
            outPut(process.getErrorStream(), Level.WARNING);
            process.waitFor();
            return null;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private void outPut(InputStream stream, Level level) throws Exception {
        String line;
        InputStreamReader isr = new InputStreamReader(stream);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader rdr = new BufferedReader(isr);
        while ((line = rdr.readLine()) != null) {
            stringBuilder.append(line);
        }
        if (level.equals(Level.INFO)) {
            logger.info(stringBuilder.toString());
        }
        if (level.equals(Level.WARNING)) {
            logger.warn(stringBuilder.toString());
        }
    }
}

