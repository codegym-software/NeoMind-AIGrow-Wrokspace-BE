package com.springboot.demo.config;

import com.springboot.demo.upload.StorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final StorageProperties props;

    public StaticResourceConfig(StorageProperties props) { this.props = props; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path base = Path.of(props.getBaseDir());
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + base.toAbsolutePath().toString() + "/")
                .setCachePeriod(3600);
    }
}
