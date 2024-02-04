package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MuseScore {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Staff")
    List<MuseStaff> staffs;

    public void setStaffs(List<MuseStaff> value) {
        if (staffs == null) {
            staffs = new ArrayList<>(value.size());
        }
        staffs.addAll(value);
    }
}
