package io.github.geniot.octavian.converter.tools;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MuseConverterOld {
    static Logger logger = LoggerFactory.getLogger(MuseConverterOld.class);

    private static final Map<String, String> CONV_MAP = new HashMap<>();

    static {
        CONV_MAP.put("//pageEvenLeftMargin/text()", "0");
        CONV_MAP.put("//pageOddLeftMargin/text()", "0");
        CONV_MAP.put("//pageEvenTopMargin/text()", "0");
        CONV_MAP.put("//pageOddTopMargin/text()", "0");
        CONV_MAP.put("//pageEvenBottomMargin/text()", "0");
        CONV_MAP.put("//pageOddBottomMargin/text()", "0");

        CONV_MAP.put("//pagePrintableWidth/text()", "1000");
        CONV_MAP.put("//pageWidth/text()", "1000");
    }

    public static void main(String[] args) {
        try {
            MuseConverterOld museConverterOld = new MuseConverterOld();

            File outDir = new File("tmp/out");
            FileUtils.deleteDirectory(outDir);

            File inputFile = new File("tmp/input.mscz");
            File tweakedInputFile = new File("tmp/input_tweaked.mscz");
            FileUtils.delete(tweakedInputFile);

            File zipCopyFile = new File(outDir.getAbsolutePath() + File.separator + "out.zip");
            FileUtils.copyFile(inputFile, zipCopyFile);

            museConverterOld.tweak(zipCopyFile);

            FileUtils.copyFile(zipCopyFile, tweakedInputFile);

            museConverterOld.runProcess(new String[]{"MuseScore4", "-o",
                    outDir.getAbsolutePath() + "/out.svg",
                    tweakedInputFile.getAbsolutePath()});

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void tweak(File input) throws Exception {
        Path zipFilePath = Paths.get(input.getAbsolutePath());
        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, this.getClass().getClassLoader())) {
            Path source = fs.getPath("/score_style.mss");
            Path temp = fs.getPath("/score_style.mss." + System.currentTimeMillis());
            Files.move(source, temp);
            streamCopy(temp, source);
            Files.delete(temp);
        }
    }

    private void streamCopy(Path src, Path dst) throws Exception {

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Files.newInputStream(src));


        for (String key : CONV_MAP.keySet()) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(key, doc, XPathConstants.NODESET);
            for (int idx = 0; idx < nodes.getLength(); idx++) {
                nodes.item(idx).setNodeValue(CONV_MAP.get(key));
            }
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        OutputStream outputStream = Files.newOutputStream(dst);
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        outputStream.close();
    }

    public Void runProcess(String[] commandLine) {
        try {
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
        if (stringBuilder.length() > 0) {
            if (level.equals(Level.INFO)) {
                logger.trace(stringBuilder.toString());
            }
            if (level.equals(Level.WARNING)) {
                logger.warn(stringBuilder.toString());
            }
        }
    }
}
