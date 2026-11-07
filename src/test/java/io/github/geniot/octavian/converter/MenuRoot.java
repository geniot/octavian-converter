package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;

@Data
@JsonRootName(value = "menu")
public class MenuRoot {

    @JsonProperty(value = "menuItems")
    List<MenuItem> menuItems;
}
