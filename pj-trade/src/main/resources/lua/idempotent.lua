-- KEYS[1] = idempotent:{userId}:{token}
-- 返回 1=成功消费 0=重复请求
if redis.call("GET", KEYS[1]) then
    redis.call("DEL", KEYS[1])
    return 1
end
return 0
