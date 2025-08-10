package com.infomationsecurity.mfa.service.impl;

import com.project.hrm.common.service.RedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Sets the specified value to the specified key in Redis.
     *
     * @param key   the key to set the value for
     * @param value the value to be set
     */
    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Sets the specified value to the specified key in Redis with an expiration time.
     *
     * @param key     the key to set the value for
     * @param value   the value to be set
     * @param timeout the duration after which the key will expire
     */
    @Override
    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * Retrieves the value associated with the specified key from Redis.
     *
     * @param key the key to retrieve the value for
     * @return the value associated with the key, or null if the key does not exist
     */
    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Retrieves the value associated with the specified key from Redis and casts it to the specified class.
     * <p>
     * Note: This method assumes that the value can be cast to the specified class. If not, a {@link ClassCastException} will be thrown.
     *
     * @param key   the key to retrieve the value for
     * @param clazz the class to cast the value to
     * @param <T>   the type of the value
     * @return the value associated with the key cast to the specified class, or null if the key does not exist
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    /**
     * Deletes the specified key from Redis.
     *
     * @param key the key to delete
     */
    @Override
    public void del(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Checks if the specified key exists in Redis.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Sets an expiration time for the specified key in Redis.
     *
     * @param key     the key to set the expiration for
     * @param timeout the duration after which the key will expire
     */
    @Override
    public void expire(String key, Duration timeout) {
        redisTemplate.expire(key, timeout);
    }

    /**
     * Adds the specified value to the end of the list stored at the specified key in Redis.
     *
     * @param key   the key of the list
     * @param value the value to add to the list
     */
    @Override
    public void listPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * Adds the specified values to the end of the list stored at the specified key in Redis.
     *
     * @param key    the key of the list
     * @param values the collection of values to add to the list
     */
    @Override
    public void listPushAll(String key, Collection<Object> values) {
        redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * Retrieves a range of elements from the list stored at the specified key in Redis.
     *
     * @param key   the key of the list
     * @param start the starting index (inclusive)
     * @param end   the ending index (inclusive)
     * @return a list of elements in the specified range
     */
    @Override
    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * Sets the specified field to the specified value in the hash stored at the specified key in Redis.
     *
     * @param key   the key of the hash
     * @param field the field to set
     * @param value the value to set for the field
     */
    @Override
    public void hashSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Retrieves the value of the specified field from **field** from the hash stored at the specified key in Redis.
     *
     * @param key   the key of the hash
     * @param field the field to retrieve
     * @return the value of the field, or null if the field does not exist
     */
    @Override
    public Object hashGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Retrieves all fields and values from the hash stored at the specified key in Redis.
     *
     * @param key the key of the hash
     * @return a map containing all fields and their values in the hash
     */
    @Override
    public Map<Object, Object> hashGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Deletes all keys matching the specified pattern from Redis.
     *
     * @param pattern the pattern to match keys against
     */
    @Override
    public void deletePattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
