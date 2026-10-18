package io.github.geniot.octavian.converter.tools;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import io.github.geniot.octavian.converter.model.Instrument;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import io.sf.carte.echosvg.transcoder.TranscoderException;
import io.sf.carte.echosvg.transcoder.TranscoderInput;
import io.sf.carte.echosvg.transcoder.TranscoderOutput;
import io.sf.carte.echosvg.transcoder.TranscodingHints;
import io.sf.carte.echosvg.transcoder.image.PNGTranscoder;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.geniot.octavian.converter.tools.VerovioHandler.crop;


@Data
@Component
public class VerovioConverter {

    VerovioHandler verovioHandler = new VerovioHandler();
    SvgHandler svgHandler = new SvgHandler();


    public MuseConversionResponse convert(String musicXml,
                                          int pngHeight,
                                          String author,
                                          String title,
                                          Instrument instrument) throws Exception {
        //MusicXML
        IOUtils.write(musicXml, new FileOutputStream("tmp/out.xml"), StandardCharsets.UTF_8);

        //MEI
        List<String> args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "mei", "--input-from", "xml"));
        String meiExpanded = new String(runProcess(args, musicXml.getBytes(StandardCharsets.UTF_8), ".mei", "--outfile", true, true), StandardCharsets.UTF_8);
        String meiExpandedCorrected = verovioHandler.fixXml(meiExpanded);
        IOUtils.write(meiExpandedCorrected, new FileOutputStream("tmp/out.mei"), StandardCharsets.UTF_8);

        //TIMEMAP
        args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "timemap", "--input-from", "mei"));
        byte[] timemapBbs = runProcess(args, meiExpandedCorrected.getBytes(StandardCharsets.UTF_8), ".json", "--outfile", true, true);
        IOUtils.write(new String(timemapBbs, StandardCharsets.UTF_8), new FileOutputStream("tmp/out.json"), StandardCharsets.UTF_8);

        //MIDI
        args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "midi", "--input-from", "mei"));
        byte[] midBbs = runProcess(args, meiExpandedCorrected.getBytes(StandardCharsets.UTF_8), ".mid", "--outfile", true, true);
        IOUtils.write(midBbs, new FileOutputStream("tmp/out.mid"));

        //WAV
        args = new ArrayList<>(List.of("timidity", "-Ow"));
        byte[] wavBbs = runProcess(args, midBbs, ".wav", "-o", true, false);
        IOUtils.write(wavBbs, new FileOutputStream("tmp/out.wav"));

        //MP3
        args = new ArrayList<>(List.of("lame"));
        byte[] mp3Bbs = runProcess(args, wavBbs, ".mp3", "", false, false);
        IOUtils.write(mp3Bbs, new FileOutputStream("tmp/out.mp3"));

        //SVG
        args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--spacing-linear", "0.40", "--expand-always", "--breaks", "none", "--output-to", "svg", "--input-from", "mei"));
        String svg = new String(runProcess(args, meiExpandedCorrected.getBytes(StandardCharsets.UTF_8), ".svg", "--outfile", true, true), StandardCharsets.UTF_8);
        svg = verovioHandler.fixSvg(svg);
        IOUtils.write(svg, new FileOutputStream("tmp/out.svg"), StandardCharsets.UTF_8);

        //PNG
        byte[] pngBytes = crop(getPngBytes(svg));
        IOUtils.write(pngBytes, new FileOutputStream("tmp/out.png"));

        return null;
    }

    private static byte @NonNull [] getPngBytes(String svg) throws TranscoderException, IOException {
        long time = System.currentTimeMillis();
        PNGTranscoder pngTranscode = new PNGTranscoder();
        TranscodingHints hints = new TranscodingHints();
        hints.put(PNGTranscoder.KEY_HEIGHT, 1000F);
        pngTranscode.setTranscodingHints(hints);
        InputStream in = new ByteArrayInputStream(svg.trim().getBytes(StandardCharsets.UTF_8));
        TranscoderInput tIn = new TranscoderInput(in);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TranscoderOutput tOut = new TranscoderOutput(os);
        pngTranscode.transcode(tIn, tOut);
        os.flush();
        os.close();
        byte[] bbs = os.toByteArray();
        System.out.println(System.currentTimeMillis() - time);
        return bbs;
    }


    private byte[] runProcess(List<String> commandLine, byte[] input, String outExtension, String outArgName, boolean isOutputFirst, boolean verbose) throws InterruptedException {
        try {
            Path tempInputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-input-", String.valueOf(input.hashCode()));
            Path tempOutputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-output-", input.hashCode() + outExtension);
            tempInputFile.toFile().deleteOnExit();
            tempOutputFile.toFile().deleteOnExit();
            Files.write(tempInputFile, input);

            if (isOutputFirst) {
                commandLine.add(outArgName);
                commandLine.add(tempOutputFile.toString());
                commandLine.add(tempInputFile.toString());
            } else {
                commandLine.add(tempInputFile.toString());
                commandLine.add(outArgName);
                commandLine.add(tempOutputFile.toString());
            }

            NuProcessBuilder pb = new NuProcessBuilder(commandLine);
            VerovioProcessHandler handler = new VerovioProcessHandler(verbose);
            pb.setProcessListener(handler);
            NuProcess process = pb.start();
            process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite

            byte[] result = Files.readAllBytes(tempOutputFile);

            Files.delete(tempInputFile);
            Files.delete(tempOutputFile);

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }
}
