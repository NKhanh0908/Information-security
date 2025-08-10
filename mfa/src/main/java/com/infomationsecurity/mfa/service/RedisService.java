package com.infomationsecurity.mfa.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public interface RedisService {
    // TODO: Working with key-value (String)
    void set(String key, Object value);

    void set(String key, Object value, Duration timeout);

    Object get(String key);

    <T> T get(String key, Class<T> clazz);

    void del(String key);

    boolean hasKey(String key);

    void expire(String key, Duration timeout);

    // TODO: Working with list
    void listPush(String key, Object value);

    void listPushAll(String key, Collection<Object> values);

    List<Object> listRange(String key, long start, long end);

    // TODO: Working with hash
    void hashSet(String key, String field, Object value);

    Object hashGet(String key, String field);

    Map<Object, Object> hashGetAll(String key);

    // TODO: Delete by pattern
    void deletePattern(String pattern);

}
