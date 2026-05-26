package com.aiqa.service.impl;

import com.aiqa.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
public class CacheServiceFallbackImpl implements CacheService {

    @Override
    public void set(String key, Object value, long timeout) {
        log.debug("Redis已禁用，跳过缓存写入: {}", key);
    }

    @Override
    public Object get(String key) {
        log.debug("Redis已禁用，返回null: {}", key);
        return null;
    }

    @Override
    public void delete(String key) {
        log.debug("Redis已禁用，跳过缓存删除: {}", key);
    }

    @Override
    public boolean hasKey(String key) {
        log.debug("Redis已禁用，返回false: {}", key);
        return false;
    }
}
