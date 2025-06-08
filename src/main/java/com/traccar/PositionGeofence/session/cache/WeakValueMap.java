package com.traccar.PositionGeofence.session.cache;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class WeakValueMap<K, V> {

    private final Map<K, WeakReference<V>> map = new HashMap<>();

    public void put(K key, V value) {
        map.put(key, new WeakReference<>(value));
    }

    public V get(K key) {
        WeakReference<V> weakReference = map.get(key);
        return (weakReference != null) ? weakReference.get() : null;
    }

    public V remove(K key) {
        WeakReference<V> weakReference = map.remove(key);
        return (weakReference != null) ? weakReference.get() : null;
    }

    private void clean() {
        map.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

}