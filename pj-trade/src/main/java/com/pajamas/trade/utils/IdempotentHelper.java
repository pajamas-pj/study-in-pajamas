package com.pajamas.trade.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 幂等校验工具类
 * <p>
 * 使用 Redis + Lua 脚本保证原子性：
 * 若 key 存在则删除并返回 1（表示首次提交，允许通过）
 * 若 key 不存在则返回 0（表示重复提交，拦截）
 * </p>
 */
@Component
@RequiredArgsConstructor
public class IdempotentHelper {

    private final StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> IDEMPOTENT_SCRIPT = new DefaultRedisScript<>();
    @PostConstruct
    public void init() {
        IDEMPOTENT_SCRIPT.setLocation(new ClassPathResource("lua/idempotent.lua"));
        IDEMPOTENT_SCRIPT.setResultType(Long.class);
    }

    public String getIdempotentToken(Long userId, String preFix) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = "idempotent:" + preFix + userId + ":" + token;
        stringRedisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);
        return token;
    }

    public Boolean checkIdempotentToken(Long userId, String token, String preFix) {
        String key = "idempotent:" + preFix + userId + ":" + token;
        Long result = stringRedisTemplate.execute(IDEMPOTENT_SCRIPT, List.of(key));
        return result != null && result == 1;
    }
}