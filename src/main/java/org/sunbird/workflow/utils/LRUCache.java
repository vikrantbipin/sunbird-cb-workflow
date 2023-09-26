package org.sunbird.workflow.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;
    private final long maxAgeInMillis;
    private final Map<K, Long> entryTimeMap;

    public LRUCache(int maxSize, long maxAgeInMillis) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
        this.maxAgeInMillis = maxAgeInMillis;
        this.entryTimeMap = new HashMap<>();
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean isMaxSizeExceeded = size() > maxSize;
        if (isMaxSizeExceeded || isEntryExpired(eldest.getKey())) {
            entryTimeMap.remove(eldest.getKey());
            return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value != null) {
            entryTimeMap.put((K) key, System.currentTimeMillis());
        }
        return value;
    }

    @Override
    public V put(K key, V value) {
        entryTimeMap.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    private boolean isEntryExpired(K key) {
        if (!entryTimeMap.containsKey(key)) {
            return true;  // Entry does not exist, considered expired
        }
        long entryTime = entryTimeMap.get(key);
        long currentTime = System.currentTimeMillis();
        return (currentTime - entryTime) > maxAgeInMillis;
    }

    public static void main(String[] args) throws InterruptedException {
        int maxSize = 3;
        long maxAgeInMillis = 60 * 1000;  // 1 minute

        LRUCache<String, Integer> cache = new LRUCache<>(maxSize, maxAgeInMillis);

        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);

        System.out.println(cache);  // Output: {A=1, B=2, C=3}

        // Sleep for 2 minutes to allow entries to expire
        Thread.sleep(2 * 60 * 1000);

        // Access A to reset its access time
        cache.get("A");

        // Adding D after entries have expired, should trigger eviction of A
        cache.put("D", 4);

        System.out.println("Sai"+ cache);  // Output: {B=2, C=3, D=4}
    }
}
