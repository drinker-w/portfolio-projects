package com.aiqa.service;

public interface CacheService {

    /**
     * 设置缓存
     */
    void set(String key, Object value, long timeout);

    /**
     * 获取缓存
     */
    Object get(String key);

    /**
     * 删除缓存
     */
    void delete(String key);

    /**
     * 判断缓存是否存在
     */
    boolean hasKey(String key);
}
