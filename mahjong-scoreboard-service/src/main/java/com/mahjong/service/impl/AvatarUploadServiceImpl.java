package com.mahjong.service.impl;

import com.mahjong.service.AvatarUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarUploadServiceImpl implements AvatarUploadService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarUploadServiceImpl.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    @Value("${app.upload.avatar-dir:./uploads/avatar}")
    private String avatarDir;

    @Value("${app.upload.avatar-url-prefix:http://localhost:8082/static/avatar}")
    private String avatarUrlPrefix;

    @Value("${app.upload.avatar-max-size:2097152}")
    private long maxSizeBytes;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("图片大小不能超过 " + (maxSizeBytes / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WebP 格式的图片");
        }
        String ext = getExtension(contentType);
        String filename = UUID.randomUUID().toString() + "." + ext;
        Path dir = Paths.get(avatarDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            logger.error("创建头像目录失败: {}", dir, e);
            throw new IllegalStateException("无法创建上传目录", e);
        }
        Path target = dir.resolve(filename);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            logger.error("保存头像文件失败: {}", target, e);
            throw new IllegalStateException("保存文件失败", e);
        }
        String urlPrefix = avatarUrlPrefix.endsWith("/") ? avatarUrlPrefix : avatarUrlPrefix + "/";
        String url = urlPrefix + filename;
        logger.info("头像上传成功: {} -> {}", file.getOriginalFilename(), url);
        return url;
    }

    private String getExtension(String contentType) {
        if (contentType == null) return "jpg";
        String lower = contentType.toLowerCase();
        if (lower.contains("png")) return "png";
        if (lower.contains("webp")) return "webp";
        return "jpg";
    }

    @Override
    public String convertTempAvatarUrlToHttps(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return null;
        }
        String url = avatarUrl.trim();
        // wxfile:// 为小程序本地路径，服务端无法访问
        if (url.startsWith("wxfile://")) {
            logger.warn("临时头像为 wxfile://，无法从服务端拉取，已忽略: {}", url);
            return null;
        }
        // 已是正常地址（非临时）则原样返回
        if (!url.contains("://tmp/")) {
            return url;
        }
        // http(s)://tmp/ 尝试下载并保存到本机，返回 https 地址
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "MahjongScoreboard/1.0");
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );
            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                logger.warn("临时头像下载结果为空: {}", url);
                return null;
            }
            if (body.length > maxSizeBytes) {
                logger.warn("临时头像超过大小限制: {} bytes", body.length);
                return null;
            }
            String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            String ext = getExtension(contentType);
            String filename = UUID.randomUUID().toString() + "." + ext;
            Path dir = Paths.get(avatarDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.write(target, body);
            String urlPrefix = avatarUrlPrefix.endsWith("/") ? avatarUrlPrefix : avatarUrlPrefix + "/";
            String resultUrl = urlPrefix + filename;
            logger.info("临时头像已转化: {} -> {}", url, resultUrl);
            return resultUrl;
        } catch (Exception e) {
            logger.warn("临时头像下载或保存失败，已忽略: {} - {}", url, e.getMessage());
            return null;
        }
    }
}
