package com.sunkaisens.ibss.common.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sunkaisens.ibss.common.domain.IBSSConstant;
import com.sunkaisens.ibss.common.service.RedisService;
import com.sunkaisens.ibss.common.utils.DateUtil;

import java.time.LocalDateTime;

/**
 * 主要用于定时删除 Redis中 key为 ibss.user.active 中
 * 已经过期的 score
 */
@Slf4j
@Component
public class CacheTask {

    @Autowired
    private RedisService redisService;

    @Scheduled(fixedRate = 3600000)
    public void run() {
        try {
            String now = DateUtil.formatFullTime(LocalDateTime.now());
            redisService.zremrangeByScore(IBSSConstant.ACTIVE_USERS_ZSET_PREFIX, "-inf", now);
            log.info("delete expired user");
        } catch (Exception ignore) {
        }
    }
}
