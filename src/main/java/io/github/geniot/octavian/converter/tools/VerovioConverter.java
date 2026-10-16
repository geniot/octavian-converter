package io.github.geniot.octavian.converter.tools;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import io.github.geniot.octavian.converter.model.Instrument;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
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


    public MuseConversionResponse convert(String musixXml,
                                          int pngHeight,
                                          String author,
                                          String title,
                                          Instrument instrument) throws Exception {
        IOUtils.write(musixXml, new FileOutputStream("tmp/out.xml"), StandardCharsets.UTF_8);

        String meiExpanded = new String(runProcess((new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "mei", "--input-from", "xml"))), musixXml, ".mei", "--outfile"), StandardCharsets.UTF_8);
        String meiExpandedCorrected = verovioHandler.fixXml(meiExpanded);
        IOUtils.write(meiExpandedCorrected, new FileOutputStream("tmp/out.mei"), StandardCharsets.UTF_8);

        String svg = new String(runProcess((new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--spacing-linear", "0.50", "--expand-always", "--breaks", "none", "--output-to", "svg", "--input-from", "mei"))), meiExpandedCorrected, ".svg", "--outfile"), StandardCharsets.UTF_8);
//        String svgCorrected = verovioHandler.fixSvg(svg);
        IOUtils.write(svg, new FileOutputStream("tmp/out.svg"), StandardCharsets.UTF_8);

        byte[] pngBytes = runProcess((new ArrayList<>(List.of("rsvg-convert"))), svg, ".png", "-o");
        IOUtils.write(pngBytes, new FileOutputStream("tmp/out.png"));

        return null;
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
