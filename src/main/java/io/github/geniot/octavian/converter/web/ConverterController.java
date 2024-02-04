package io.github.geniot.octavian.converter.web;

import io.github.geniot.octavian.converter.model.MuseConversionRequest;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConverterController {
    @PostMapping("/convert")
    public @ResponseBody
    MuseConversionResponse handleRefreshSubscription(@RequestBody MuseConversionRequest request) {
        MuseConversionResponse result = new MuseConversionResponse();
        return result;
    }
}
