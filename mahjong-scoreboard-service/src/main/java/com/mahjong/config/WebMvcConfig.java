package com.mahjong.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源映射：将 /static/avatar/** 映射到头像上传目录，使返回的头像 URL 可被公网访问。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir:./uploads/avatar}")
    private String avatarDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(avatarDir).toAbsolutePath().normalize();
        String pathPattern = "file:" + dir.toString().replace("\\", "/") + "/";
        registry.addResourceHandler("/static/avatar/**")
                .addResourceLocations(pathPattern);
    }
}
