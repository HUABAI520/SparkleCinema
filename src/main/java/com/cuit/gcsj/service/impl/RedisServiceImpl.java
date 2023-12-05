package com.cuit.gcsj.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author L
 */
@Component
public class RedisServiceImpl {
    @Resource
    private  RedisTemplate<String, String> redisTemplate;

    // 将文件内容存入 Redis
    public void saveMoviesData(List<String> moviesData, String key) {
        redisTemplate.opsForList().rightPushAll(key, moviesData.toArray(new String[0]));
        redisTemplate.expire(key, 1, TimeUnit.SECONDS);
    }
    // 获取 Redis 中的数据
    public List<String> getMoviesData(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
