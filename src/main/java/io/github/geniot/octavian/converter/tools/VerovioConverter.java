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


    public MuseConversionResponse convert(String xml,
                                          int pngHeight,
                                          String author,
                                          String title,
                                          Instrument instrument) throws Exception {
        List<String> args;
        //XML
        IOUtils.write(xml, new FileOutputStream("tmp/out.xml"), StandardCharsets.UTF_8);

        //XML->JSON
        args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "timemap", "--input-from", "xml"));
        byte[] json = runProcess(args, xml.getBytes(StandardCharsets.UTF_8), ".xml", ".json", "--outfile", true, true);
        IOUtils.write(new String(json, StandardCharsets.UTF_8), new FileOutputStream("tmp/out.json"), StandardCharsets.UTF_8);

        //XML->MIDI
        args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        byte[] midi = runProcess(args, xml.getBytes(StandardCharsets.UTF_8), ".xml", ".mid", "-o", true, true);
        IOUtils.write(midi, new FileOutputStream("tmp/out.mid"));

        //XML->MP3
        args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        byte[] mp3 = runProcess(args, xml.getBytes(StandardCharsets.UTF_8), ".xml", ".mp3", "-o", true, false);
        IOUtils.write(mp3, new FileOutputStream("tmp/out.mp3"));

        //XML->MEI
        args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "mei", "--input-from", "xml"));
        String mei = new String(runProcess(args, xml.getBytes(StandardCharsets.UTF_8), ".xml", ".mei", "--outfile", true, true), StandardCharsets.UTF_8);
        mei = verovioHandler.fixXml(mei);
        IOUtils.write(mei, new FileOutputStream("tmp/out.mei"), StandardCharsets.UTF_8);

        //MEI->SVG
        args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--spacing-linear", "0.40", "--expand-always", "--breaks", "none", "--output-to", "svg", "--input-from", "mei"));
        String svg = new String(runProcess(args, mei.getBytes(StandardCharsets.UTF_8), ".mei", ".svg", "--outfile", true, true), StandardCharsets.UTF_8);
        svg = verovioHandler.fixSvg(svg);
        IOUtils.write(svg, new FileOutputStream("tmp/out.svg"), StandardCharsets.UTF_8);

        //SVG->PNG
        byte[] png = crop(getPngBytes(svg));
        IOUtils.write(png, new FileOutputStream("tmp/out.png"));

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


    private byte[] runProcess(List<String> commandLine, byte[] input, String inExtension, String outExtension, String outArgName, boolean isOutputFirst, boolean verbose) throws InterruptedException {
        try {
            Path tempInputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-input-", String.valueOf(input.hashCode() + inExtension));
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
