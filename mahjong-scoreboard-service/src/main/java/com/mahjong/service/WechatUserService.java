package com.mahjong.service;

import com.mahjong.model.WechatUser;
import java.util.List;
import java.util.Optional;

public interface WechatUserService {
    WechatUser createWechatUser(WechatUser wechatUser);
    Optional<WechatUser> getWechatUserById(Long id);
    WechatUser getWechatUserByUserId(String userId);
    List<WechatUser> getAllWechatUsers();
    WechatUser updateWechatUser(Long id, WechatUser wechatUser);
    void deleteWechatUser(Long id);
    boolean existsByUserId(String userId);
}