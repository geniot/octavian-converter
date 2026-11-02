package io.github.geniot.octavian.converter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.SortedSet;

public class PngHandler {
    public BufferedImage image;

    public PngHandler(byte[] png) throws Exception {
        this.image = ImageIO.read(new ByteArrayInputStream(png));
        this.crop();
    }

    private void crop() {
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

        image = image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public void debugLines(SortedSet<Integer> barOffsets, Color color, BasicStroke stroke) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.setStroke(stroke);
        for (Integer pngBarOffset : barOffsets) {
            g2d.drawLine(pngBarOffset, 0, pngBarOffset, image.getHeight());
        }
    }

    public byte[] image2bytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
