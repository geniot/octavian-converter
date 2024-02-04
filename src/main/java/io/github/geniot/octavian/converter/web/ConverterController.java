package io.github.geniot.octavian.converter.web;

import io.github.geniot.octavian.converter.model.MuseConversionRequest;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

@RestController
public class ConverterController {
    @PostMapping(value = "/convert", consumes = {"application/json"})
    public @ResponseBody
    MuseConversionResponse handleRefreshSubscription(@RequestBody MuseConversionRequest request) {
        MuseConversionResponse result = new MuseConversionResponse();

        IntBuffer intBuf =
                ByteBuffer.wrap(request.getAuthor().getBytes())
                        .order(ByteOrder.BIG_ENDIAN)
                        .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);

        result.setRepeats(array);
        return result;
    }
}
