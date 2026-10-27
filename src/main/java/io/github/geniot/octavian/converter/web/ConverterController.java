package io.github.geniot.octavian.converter.web;

import io.github.geniot.octavian.converter.verovio.MuseConversionRequest;
import io.github.geniot.octavian.converter.verovio.MuseConversionResponse;
import io.github.geniot.octavian.converter.verovio.OctavianConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConverterController {
    @Autowired
    OctavianConverter octavianConverter;

    @PostMapping(value = "/convert", consumes = {"application/json"})
    public @ResponseBody
    MuseConversionResponse convertVerovio(@RequestBody MuseConversionRequest request) throws Exception {
        return octavianConverter.convert(request.getMuseXml());
    }
}
