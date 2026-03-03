package com.mahjong.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 头像上传服务：将小程序选图（wxfile://、http(s)://tmp/）上传到服务器存储，返回可公网访问的 URL。
 */
public interface AvatarUploadService {

    /**
     * 上传头像文件，保存到配置的目录并返回可公网访问的 URL。
     *
     * @param file 图片文件（通常为 multipart/form-data 的 file 字段）
     * @return 可公网访问的 https 地址，例如 https://example.com/static/avatar/xxx.jpg
     * @throws IllegalArgumentException 文件为空、类型不允许或超过大小限制时
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 将前端传的临时头像地址转化为可公网访问的 https 地址。
     * - wxfile:// 无法从服务端拉取，返回 null；
     * - http(s)://tmp/ 会从该 URL 下载图片并保存到本机，返回新的 https URL；
     * - 已是正常 URL 则原样返回。
     *
     * @param avatarUrl 前端传来的头像地址（可能为临时路径）
     * @return 可持久化的 https 地址，无法转化时返回 null
     */
    String convertTempAvatarUrlToHttps(String avatarUrl);
}
