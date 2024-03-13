package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.tools.SvgHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ConvertSvgToPng {
    public static void main(String[] args) {
        try {
            SvgHandler svgHandler = new SvgHandler();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResource("score.svg").openStream(), byteArrayOutputStream);
            long t1 = System.currentTimeMillis();
            byte[] bbs = svgHandler.svg2png(byteArrayOutputStream.toByteArray(), 42852.5f, 539.553f);
            System.out.println(System.currentTimeMillis() - t1);
            FileUtils.writeByteArrayToFile(new File("out.png"), bbs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
