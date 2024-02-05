package io.github.geniot.octavian.converter.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    public static String getMuseXml(byte[] inputFileBytes) throws Exception {
        ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(inputFileBytes));
        ZipEntry entry;
        byte[] buffer = new byte[2048];
        while ((entry = stream.getNextEntry()) != null) {
            if (entry.getName().endsWith(".mscx")) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                int len = 0;
                while ((len = stream.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
                output.close();
                return output.toString(StandardCharsets.UTF_8);
            }
        }
        throw new Exception("Couldn't find mscx file in the mscz archive.");
    }

    public static byte[] decompressBytes(final byte[] data)
            throws IOException {
        if (data == null || data.length == 0) {
            return null;
        } else {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] tmp = new byte[256];
            while (true) {
                int r = is.read(tmp);
                if (r < 0) {
                    break;
                }
                buffer.write(tmp, 0, r);
            }
            is.close();
            return buffer.toByteArray();
        }
    }
}
