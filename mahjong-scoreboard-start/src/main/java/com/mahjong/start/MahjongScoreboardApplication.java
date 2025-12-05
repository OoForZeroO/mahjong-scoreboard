package com.mahjong.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.mahjong")
@EntityScan(basePackages = "com.mahjong.model")
@EnableJpaRepositories(basePackages = "com.mahjong.repository")
@EnableScheduling
public class MahjongScoreboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(MahjongScoreboardApplication.class, args);
    }

}