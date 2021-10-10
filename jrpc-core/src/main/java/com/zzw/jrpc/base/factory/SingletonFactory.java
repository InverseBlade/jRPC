package com.zzw.jrpc.base.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {
    private static final Map<String, Object> CONTAINERS = new ConcurrentHashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException();
        }
        String k = clazz.toString();
        if (CONTAINERS.containsKey(k)) {
            return clazz.cast(CONTAINERS.get(k));
        }
        Object obj = CONTAINERS.computeIfAbsent(k, key -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        return clazz.cast(obj);
    }

}
