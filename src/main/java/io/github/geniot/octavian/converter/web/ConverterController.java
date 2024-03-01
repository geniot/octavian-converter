package io.github.geniot.octavian.converter.web;

import io.github.geniot.octavian.converter.model.MuseConversionRequest;
import io.github.geniot.octavian.converter.model.MuseConversionResponse;
import io.github.geniot.octavian.converter.tools.MuseConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConverterController {
    @Autowired
    MuseConverter museConverter;

    @PostMapping(value = "/convert", consumes = {"application/json"})
    public @ResponseBody
    MuseConversionResponse handleRefreshSubscription(@RequestBody MuseConversionRequest request) throws Exception {
        return museConverter.convert(request.getMuseXml(), request.getPngHeight(), request.getAuthor(), request.getTitle(), request.getInstrument(), true);
    }
}
