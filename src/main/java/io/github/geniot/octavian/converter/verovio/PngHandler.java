package io.github.geniot.octavian.converter.verovio;

import io.sf.carte.echosvg.transcoder.TranscoderInput;
import io.sf.carte.echosvg.transcoder.TranscoderOutput;
import io.sf.carte.echosvg.transcoder.TranscodingHints;
import io.sf.carte.echosvg.transcoder.image.PNGTranscoder;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.SortedSet;

public class PngHandler {
    public byte @NonNull [] getPngBytes(byte[] svg) throws Exception {
        long time = System.currentTimeMillis();
        PNGTranscoder pngTranscode = new PNGTranscoder();
        TranscodingHints hints = new TranscodingHints();
        hints.put(PNGTranscoder.KEY_HEIGHT, 1000F);
        pngTranscode.setTranscodingHints(hints);
        InputStream in = new ByteArrayInputStream(svg);
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

    public Dimension getPngDimension(byte[] bbs) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bbs));
        return new Dimension(image.getWidth(), image.getHeight());
    }

    public byte[] crop(byte[] bbs) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bbs));
        image = crop(image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public BufferedImage crop(BufferedImage image) {
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

    public byte[] svg2png(byte[] svg) throws Exception {
        return crop(getPngBytes(svg));
    }

    public byte[] debugLines(byte[] bbs, SortedSet<Integer> barOffsets, Color color, BasicStroke stroke) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bbs));
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.setStroke(stroke);
        for (Integer pngBarOffset : barOffsets) {
            g2d.drawLine(pngBarOffset, 0, pngBarOffset, image.getHeight());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
