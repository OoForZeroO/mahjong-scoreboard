package com.mahjong.service.impl;

import com.mahjong.model.WechatUser;
import com.mahjong.repository.WechatUserRepository;
import com.mahjong.service.WechatUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("wechatUserService")
public class WechatUserServiceHandler implements WechatUserService {

    @Autowired
    private WechatUserRepository dao;

    @Override
    public WechatUser createWechatUser(WechatUser u) {
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
                existing.setAvatar(u.getAvatar());
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