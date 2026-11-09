package io.github.geniot.octavian.converter;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import io.sf.carte.echosvg.transcoder.TranscoderInput;
import io.sf.carte.echosvg.transcoder.TranscoderOutput;
import io.sf.carte.echosvg.transcoder.TranscodingHints;
import io.sf.carte.echosvg.transcoder.image.PNGTranscoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProcessStarter {
    private final boolean isVerbose = false;

    public String xml2mei(String xml) throws Exception {
        List<String> args = new ArrayList<>(Arrays.asList(
                "./verovio",
                "-r", "data",
                "--expand-always",
                "--breaks", "none",
                "--output-to", "mei",
                "--input-from", "xml"
        ));
        return new String(runProcess(args, xml.getBytes(StandardCharsets.UTF_8), ".xml", ".mei", "--outfile", true, isVerbose), StandardCharsets.UTF_8);
    }

    public String mei2svg(String mei) throws Exception {
        List<String> args = new ArrayList<>(Arrays.asList("./verovio",
                "-r", "data",
                "--svg-bounding-boxes", "--svg-content-bounding-boxes",
//                "--svg-html5", "--svg-remove-xlink",
//                "--scale", "150",
                "--adjust-page-height", "--adjust-page-width",
                "--page-margin-bottom", "0", "--page-margin-top", "0", "--page-margin-left", "26", "--page-margin-right", "0",
                "--spacing-linear", "0.45",
                "--breaks", "none",
                "--output-to", "svg",
                "--input-from", "mei"
        ));
        return new String(runProcess(args, mei.getBytes(StandardCharsets.UTF_8), ".mei", ".svg", "--outfile", true, isVerbose), StandardCharsets.UTF_8);
    }

    public byte[] xml2midi(String xml) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        return runProcess(args, xml.getBytes(StandardCharsets.UTF_8), ".xml", ".mid", "-o", true, isVerbose);
    }

    public String midi2json(byte[] midiBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("./midi2json"));
        return new String(runProcess(args, midiBbs, ".mid", ".json", "", false, isVerbose), StandardCharsets.UTF_8);
    }

    public byte[] getPngBytesEchoSvg(String svg) throws Exception {
        long time = System.currentTimeMillis();
        PNGTranscoder pngTranscode = new PNGTranscoder();
        TranscodingHints hints = new TranscodingHints();
//        hints.put(PNGTranscoder.KEY_HEIGHT, 1000F);
        pngTranscode.setTranscodingHints(hints);
        InputStream in = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
        TranscoderInput tIn = new TranscoderInput(in);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TranscoderOutput tOut = new TranscoderOutput(os);
        pngTranscode.transcode(tIn, tOut);
        os.flush();
        os.close();
        byte[] bbs = os.toByteArray();
        log.debug("SVG->PNG took: {}", System.currentTimeMillis() - time);
        return bbs;
    }

    public byte[] getPngBytesJSVG(String svg) throws Exception {
        svg = svg.replaceFirst("<style type=\"text/css\">", "<style type=\"text/css\">[stroke-width=\"0\"] { stroke: none !important };");
        long time = System.currentTimeMillis();
        SVGLoader loader = new SVGLoader();
        ByteArrayInputStream is = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
        SVGDocument svgDocument = loader.load(is, null, LoaderContext.createDefault());

        FloatSize size = svgDocument.size();
        BufferedImage image = new BufferedImage((int) size.width, (int) size.height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        svgDocument.render(null, g);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bbs = baos.toByteArray();
        System.out.println("SVG->PNG took: " + (System.currentTimeMillis() - time));
        return bbs;
    }

    private byte[] runProcess(
            List<String> commandLine,
            byte[] input,
            String inExtension,
            String outExtension,
            String outArgName,
            boolean isOutputFirst,
            boolean verbose
    ) throws Exception {

        Path tempInputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-input-", String.valueOf(input.hashCode() + inExtension));
        Path tempOutputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-output-", input.hashCode() + outExtension);
        tempInputFile.toFile().deleteOnExit();
        tempOutputFile.toFile().deleteOnExit();
        Files.write(tempInputFile, input);

        if (isOutputFirst) {
            if (!StringUtils.isEmpty(outArgName)) {
                commandLine.add(outArgName);
            }
            commandLine.add(tempOutputFile.toString());
            commandLine.add(tempInputFile.toString());
        } else {
            commandLine.add(tempInputFile.toString());
            if (!StringUtils.isEmpty(outArgName)) {
                commandLine.add(outArgName);
            }
            commandLine.add(tempOutputFile.toString());
        }

        NuProcessBuilder pb = new NuProcessBuilder(commandLine);
        ProcessHandler handler = new ProcessHandler(verbose);
        pb.setProcessListener(handler);
        NuProcess process = pb.start();
        process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite

        byte[] result = Files.readAllBytes(tempOutputFile);

        Files.delete(tempInputFile);
        Files.delete(tempOutputFile);

        return result;
    }
}
