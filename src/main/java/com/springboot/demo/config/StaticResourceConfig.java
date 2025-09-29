package com.springboot.demo.config;

import com.springboot.demo.upload.StorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final StorageProperties props;

    public StaticResourceConfig(StorageProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = props.getBaseDir();
        if (baseDir != null && !baseDir.trim().isEmpty()) {
            Path base = Path.of(baseDir);
            registry.addResourceHandler("/files/**")
                    .addResourceLocations("file:" + base.toAbsolutePath() + "/")
                    .setCachePeriod(3600);
        }
    }
}
