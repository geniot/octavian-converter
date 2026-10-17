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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


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
//        byte[] mp3Bbs = runProcess((new ArrayList<>(Arrays.asList("xvfb-run", "-a", "-e", "/dev/stdout", "/opt/musescore/MuseScore-Studio-4.7.5.260831071-x86_64.AppImage", "mp3"))), musicXml, ".mp3", "--export-to");
//        IOUtils.write(mp3Bbs, new FileOutputStream("tmp/out.mp3"));

        IOUtils.write(musicXml, new FileOutputStream("tmp/out.xml"), StandardCharsets.UTF_8);

        String meiExpanded = new String(runProcess((new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "mei", "--input-from", "xml"))), musicXml, ".mei", "--outfile"), StandardCharsets.UTF_8);
        String meiExpandedCorrected = verovioHandler.fixXml(meiExpanded);
        IOUtils.write(meiExpandedCorrected, new FileOutputStream("tmp/out.mei"), StandardCharsets.UTF_8);

        byte[] midBbs = runProcess((new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "midi", "--input-from", "xml"))), musicXml, ".mid", "--outfile");
        IOUtils.write(midBbs, new FileOutputStream("tmp/out.mid"));

//        byte[] wavBbs = runProcess((new ArrayList<>(Arrays.asList("timidity", "--input-from", "xml"))), musicXml, ".mid", "--outfile");
//        IOUtils.write(midBbs, new FileOutputStream("tmp/out.mid"));

        String svg = new String(runProcess((new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--spacing-linear", "0.40", "--expand-always", "--breaks", "none", "--output-to", "svg", "--input-from", "mei"))), meiExpandedCorrected, ".svg", "--outfile"), StandardCharsets.UTF_8);
        svg = verovioHandler.fixSvg(svg);
        IOUtils.write(svg, new FileOutputStream("tmp/out.svg"), StandardCharsets.UTF_8);

        byte[] pngBytes = crop(getPngBytes(svg));
//        byte[] pngBytes = JairoSVG.svg2png(svg.getBytes(StandardCharsets.UTF_8));
//        byte[] pngBytes = runProcess((new ArrayList<>(List.of("rsvg-convert"))), svg, ".png", "-o");
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

    public static byte[] crop(byte[] bbs) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bbs));
        image = crop(image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public static BufferedImage crop(BufferedImage image) {
        int minY = 0, maxY = 0, minX = Integer.MAX_VALUE, maxX = 0;
        boolean isBlank, minYIsDefined = false;
        Raster raster = image.getRaster();

        for (int y = 0; y < image.getHeight(); y++) {
            isBlank = true;

            for (int x = 0; x < image.getWidth(); x++) {
                //Change condition to (raster.getSample(x, y, 3) != 0)
                //for better performance
                if (raster.getPixel(x, y, (int[]) null)[3] != 0) {
                    isBlank = false;

                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                }
            }

            if (!isBlank) {
                if (!minYIsDefined) {
                    minY = y;
                    minYIsDefined = true;
                } else {
                    if (y > maxY) maxY = y;
                }
            }
        }

        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private byte[] runProcess(List<String> commandLine, String input, String outExtension, String outArgName) throws InterruptedException {
        try {
            Path tempInputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-input-", String.valueOf(input.hashCode()));
            Path tempOutputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-output-", input.hashCode() + outExtension);
            tempInputFile.toFile().deleteOnExit();
            tempOutputFile.toFile().deleteOnExit();
            Files.write(tempInputFile, input.getBytes());

            commandLine.add(outArgName);
            commandLine.add(tempOutputFile.toString());
            commandLine.add(tempInputFile.toString());

            NuProcessBuilder pb = new NuProcessBuilder(commandLine);
            VerovioProcessHandler handler = new VerovioProcessHandler();
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
