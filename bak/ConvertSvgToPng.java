package io.github.geniot.octavian.converter;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import io.github.geniot.octavian.converter.tools.SvgHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class ConvertSvgToPng {
    public static void main(String[] args) {
        try {
            SvgHandler svgHandler = new SvgHandler();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResource("score.svg").openStream(), byteArrayOutputStream);
            long t1 = System.currentTimeMillis();

            SVGLoader loader = new SVGLoader();
            SVGDocument svgDocument = loader.load(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            byte[] bbs = svgHandler.svg2png(svgDocument);

            System.out.println(System.currentTimeMillis() - t1);
            FileUtils.writeByteArrayToFile(new File("out.png"), bbs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
