package com.searchDev.SearchDev.Service.RedisService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


import java.util.Set;
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    public <T> void save(String key, T value, Duration ttl) {
        try {
            String json = mapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            throw new RuntimeException("Redis Serialization Error", e);
        }
    }

    public <T> T get(String key, TypeReference<T> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null)
                return null;
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Redis Deserialization Error", e);
        }
    }
    public void delete(String key){
        redisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern){
           Set<String> keys= redisTemplate.keys(pattern);
           if(keys!=null) redisTemplate.delete(keys);
    }

}
