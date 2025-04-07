package com.traccar.PositionGeofence.session.cache;



import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Notification;
import com.traccar.PositionGeofence.modelo.User;

public class CacheGraph {

    private final Map<CacheKey, CacheNode> roots = new HashMap<>();
    private final WeakValueMap<CacheKey, CacheNode> nodes = new WeakValueMap<>();

    void addObject(BaseModel value) {
        CacheKey key = new CacheKey(value);
        CacheNode node = new CacheNode(value);
        roots.put(key, node);
        nodes.put(key, node);
    }

    void removeObject(Class<? extends BaseModel> clazz, String id) {
        CacheKey key = new CacheKey(clazz, id);
        CacheNode node = nodes.remove(key);
        if (node != null) {
            node.getAllLinks(false).forEach(child -> child.getLinks(key.clazz(), true).remove(node));
        }
        roots.remove(key);
    }

    @SuppressWarnings("unchecked")
    <T extends BaseModel> T getObject(Class<T> clazz, String id) {
        CacheNode node = nodes.get(new CacheKey(clazz, id));
        return node != null ? (T) node.getValue() : null;
    }

    <T extends BaseModel> Stream<T> getObjects(
            Class<? extends BaseModel> fromClass, String notificationId,
            Class<Notification> clazz, Set<Class<? extends BaseModel>> proxies, boolean forward) {

        CacheNode rootNode = nodes.get(new CacheKey(fromClass, notificationId));
        if (rootNode != null) {
            return (Stream<T>) getObjectStream(rootNode, clazz, proxies, forward);
        } else {
            return Stream.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseModel> Stream<T> getObjectStream(
            CacheNode rootNode, Class<T> clazz, Set<Class<? extends BaseModel>> proxies, boolean forward) {

        if (proxies.contains(clazz)) {
            return Stream.empty();
        }

        var directSteam = rootNode.getLinks(clazz, forward).stream()
                .map(node -> (T) node.getValue());

        var proxyStream = proxies.stream()
                .flatMap(proxyClass -> rootNode.getLinks(proxyClass, forward).stream()
                        .flatMap(node -> getObjectStream(node, clazz, proxies, forward)));

        return Stream.concat(directSteam, proxyStream);
    }

    void updateObject(BaseModel value) {
        CacheNode node = nodes.get(new CacheKey(value));
        if (node != null) {
            node.setValue(value);
        }
    }

    boolean addLink(
            Class<? extends BaseModel> fromClazz, String fromId,
            BaseModel toValue) {
        boolean stop = true;
        CacheNode fromNode = nodes.get(new CacheKey(fromClazz, fromId));
        if (fromNode != null) {
            CacheKey toKey = new CacheKey(toValue);
            CacheNode toNode = nodes.get(toKey);
            if (toNode == null) {
                stop = false;
                toNode = new CacheNode(toValue);
                nodes.put(toKey, toNode);
            }
            fromNode.getLinks(toValue.getClass(), true).add(toNode);
            toNode.getLinks(fromClazz, false).add(fromNode);
        }
        return stop;
    }

    void removeLink(
            Class<? extends BaseModel> fromClazz, String fromId,
            Class<? extends BaseModel> toClazz, String toId) {
        CacheNode fromNode = nodes.get(new CacheKey(fromClazz, fromId));
        if (fromNode != null) {
            CacheNode toNode = nodes.get(new CacheKey(toClazz, toId));
            if (toNode != null) {
                fromNode.getLinks(toClazz, true).remove(toNode);
                toNode.getLinks(fromClazz, false).remove(fromNode);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (CacheNode node : roots.values()) {
            printNode(stringBuilder, node, "");
        }
        return stringBuilder.toString().trim();
    }

    private void printNode(StringBuilder stringBuilder, CacheNode node, String indentation) {
        stringBuilder
                .append('\n')
                .append(indentation)
                .append(node.getValue().getClass().getSimpleName())
                .append('(').append(node.getValue().getId()).append(')');
        node.getAllLinks(true).forEach(child -> printNode(stringBuilder, child, indentation + "  "));
    }

}