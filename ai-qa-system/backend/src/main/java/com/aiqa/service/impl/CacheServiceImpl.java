package com.aiqa.service.impl;

import com.aiqa.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class CacheServiceImpl implements CacheService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value, long timeout) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("Redis写入失败，降级处理: {}", e.getMessage());
        }
    }

    @Override
    public Object get(String key) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            log.warn("Redis读取失败，降级处理: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void delete(String key) {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("Redis删除失败，降级处理: {}", e.getMessage());
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            if (redisTemplate != null) {
                Boolean result = redisTemplate.hasKey(key);
                return Boolean.TRUE.equals(result);
            }
        } catch (Exception e) {
            log.warn("Redis判断key失败，降级处理: {}", e.getMessage());
        }
        return false;
    }
}
