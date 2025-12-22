package com.searchDev.SearchDev.Security.rateLimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class RateLimitService {
    private static final String LUA_SCRIPT = """
        -- KEYS[1] = rate limit key
        -- ARGV[1] = max tokens
        -- ARGV[2] = refill window (seconds)
        -- ARGV[3] = current timestamp
        
        local key = KEYS[1]
        local maxTokens = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local now = tonumber(ARGV[3])
        
        local data = redis.call("HMGET", key, "tokens", "timestamp")
        local tokens = tonumber(data[1])
        local lastTime = tonumber(data[2])
        
        if tokens == nil then
            tokens = maxTokens
            lastTime = now
        end
        
        local delta = now - lastTime
        local refill = math.floor(delta * maxTokens / window)
        tokens = math.min(maxTokens, tokens + refill)
        
        if tokens <= 0 then
            return 0
        else
            tokens = tokens - 1
            redis.call("HMSET", key, "tokens", tokens, "timestamp", now)
            redis.call("EXPIRE", key, window)
            return 1
        end
        """;

        private final StringRedisTemplate redisTemplate;
        private final DefaultRedisScript<Long> script;

        @Autowired
        RateLimitService(StringRedisTemplate redisTemplate){
            this.redisTemplate = redisTemplate;
            this.script = new DefaultRedisScript<>();
            this.script.setScriptText(LUA_SCRIPT);
            this.script.setResultType(Long.class);
        }


        public boolean isAllowed(
            String userId,
            String endpoint,
            int maxRequest,
            int durationSeconds
        ){
            String key = "rate:" + userId + ":" + endpoint;
            long now = System.currentTimeMillis()/1000;

            Long result = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(maxRequest),
                String.valueOf(durationSeconds),
                String.valueOf(now)
            );

            return result!=null && result==1;
        }
}
