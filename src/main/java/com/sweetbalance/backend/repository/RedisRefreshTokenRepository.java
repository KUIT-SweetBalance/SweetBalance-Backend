package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.RefreshEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

@Repository
public class RedisRefreshTokenRepository implements RefreshTokenRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "refresh_token:";

    public RedisRefreshTokenRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean existsByRefresh(String refresh) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + refresh));
    }

    @Override
    public void deleteByRefresh(String refresh) {
        redisTemplate.delete(KEY_PREFIX + refresh);
    }

    @Override
    public void save(RefreshEntity refreshEntity) {
        String key = KEY_PREFIX + refreshEntity.getRefresh();
        redisTemplate.opsForValue().set(key, refreshEntity.getEmail());
        if (refreshEntity.getExpiration() != null) {
            Duration duration = Duration.between(LocalDateTime.now(), refreshEntity.getExpiration());
            redisTemplate.expire(key, duration);
        }
    }
}
