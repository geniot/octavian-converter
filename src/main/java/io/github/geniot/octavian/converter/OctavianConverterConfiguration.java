package io.github.geniot.octavian.converter;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
@EnableCaching
@Getter
public class OctavianConverterConfiguration implements WebMvcConfigurer {
    Logger logger = LoggerFactory.getLogger(OctavianConverterConfiguration.class);
}
