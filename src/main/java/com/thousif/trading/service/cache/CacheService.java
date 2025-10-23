package com.thousif.trading.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheObject(String key, Object value, Duration ttl){
        try{
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached object with key: {}", key);
        }
        catch (Exception e){
            log.error("Failed to cache object with key: {}", key, e);
        }
    }

    public Object getCacheObject(String key){
        try{
            return redisTemplate.opsForValue().get(key);
        }
        catch (Exception e){
            log.error("Failed to retrieve cached object with key: {}", key, e);
            return null;
        }
    }

    public void evictCache(String key){
        try{
            redisTemplate.delete(key);
            log.debug("Evicted cache with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict cache with key: {}", key, e);
        }
    }

    public boolean hasKey(String key){
        try{
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Failed to check the key existence: {}", key, e);
            return false;
        }
    }

    public void publishMessage(String channel, Object message){
        try{
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published message to channel: {}", channel);
        }
        catch (Exception e){
            log.error("Failed to publish message to channel: {}", channel, e);
        }
    }
}
