package com.mahjong.repository;

import com.mahjong.model.WechatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WechatUserRepository extends JpaRepository<WechatUser, Long> {
    WechatUser findByUserId(String userId);
    boolean existsByUserId(String userId);

    WechatUser findByIdentityKey(String identityKey);
    boolean existsByIdentityKey(String identityKey);
}