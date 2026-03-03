package com.mahjong.service.impl;

import com.mahjong.model.WechatUser;
import com.mahjong.repository.WechatUserRepository;
import com.mahjong.service.AvatarUploadService;
import com.mahjong.service.WechatUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("wechatUserService")
public class WechatUserServiceHandler implements WechatUserService {

    @Autowired
    private WechatUserRepository dao;

    @Autowired
    private AvatarUploadService avatarUploadService;

    /**
     * 头像地址规范化：
     * - 去掉前后空格
     * - 丢弃 wxfile:// 和 http(s)://tmp/ 这类临时地址（返回 null）
     */
    private String sanitizeAvatar(String avatar) {
        if (avatar == null) {
            return null;
        }
        String url = avatar.trim();
        if (url.isEmpty()) {
            return null;
        }
        // 小程序本地/临时文件路径，不应持久化到数据库
        if (url.startsWith("wxfile://") || url.contains("://tmp/")) {
            return null;
        }
        return url;
    }

    /**
     * 将前端传的头像转为可持久化的 https 地址：临时地址先尝试下载并保存，非临时原样返回。
     */
    private String normalizeAvatarForSave(String avatar) {
        if (avatar == null || avatar.trim().isEmpty()) {
            return null;
        }
        String converted = avatarUploadService.convertTempAvatarUrlToHttps(avatar);
        if (converted != null) {
            return converted;
        }
        return sanitizeAvatar(avatar);
    }

    @Override
    public WechatUser createWechatUser(WechatUser u) {
        u.setAvatar(normalizeAvatarForSave(u.getAvatar()));
        return dao.save(u);
    }

    @Override
    public Optional<WechatUser> getWechatUserById(Long id) {
        return dao.findById(id);
    }

    @Override
    public WechatUser getWechatUserByUserId(String userId) {
        return dao.findByUserId(userId);
    }

    @Override
    public List<WechatUser> getAllWechatUsers() {
        return dao.findAll();
    }

    @Override
    public WechatUser updateWechatUser(Long id, WechatUser u) {
        Optional<WechatUser> existingOpt = dao.findById(id);
        if (existingOpt.isPresent()) {
            WechatUser existing = existingOpt.get();
            // 更新用户信息（如果提供）
            if (u.getNickname() != null) {
                existing.setNickname(u.getNickname());
            }
            if (u.getUsername() != null) {
                existing.setUsername(u.getUsername());
            }
            if (u.getAvatar() != null) {
                existing.setAvatar(normalizeAvatarForSave(u.getAvatar()));
            }
            if (u.getIsVisitor() != null) {
                existing.setIsVisitor(u.getIsVisitor());
            }
            return dao.save(existing);
        }
        return null;
    }

    @Override
    public void deleteWechatUser(Long id) {
        dao.deleteById(id);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return dao.existsByUserId(userId);
    }
}