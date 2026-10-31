package io.github.geniot.octavian.converter.web;

import io.github.geniot.octavian.converter.ConversionRequest;
import io.github.geniot.octavian.converter.ConversionResponse;
import io.github.geniot.octavian.converter.OctavianConverter;
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
    ConversionResponse convertVerovio(@RequestBody ConversionRequest request) throws Exception {
        return octavianConverter.convert(request.getMuseXml());
    }
}
