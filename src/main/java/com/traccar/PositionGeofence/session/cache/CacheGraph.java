package com.traccar.PositionGeofence.session.cache;



import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.traccar.PositionGeofence.modelo.BaseModel;

public class CacheGraph {

    // Mapa de raíces, donde se almacenan los nodos principales.
    private final Map<CacheKey, CacheNode> roots = new HashMap<>();
    // Mapa con valores débiles para los nodos, para evitar mantener objetos innecesariamente en memoria.
    private final WeakValueMap<CacheKey, CacheNode> nodes = new WeakValueMap<>();

    /**
     * Agrega un objeto a la cache.
     */
    public void addObject(BaseModel value) {
        CacheKey key = new CacheKey(value);
        CacheNode node = new CacheNode(value);
        roots.put(key, node);
        nodes.put(key, node);
    }

    /**
     * Elimina un objeto de la cache.
     */
    public void removeObject(Class<? extends BaseModel> clazz, long id) {
        CacheKey key = new CacheKey(clazz, id);
        CacheNode node = nodes.remove(key);
        if (node != null) {
            // Se eliminan los enlaces hacia este nodo.
            node.getAllLinks(false).forEach(child -> child.getLinks(key.clazz(), true).remove(node));
        }
        roots.remove(key);
    }

    /**
     * Recupera un objeto almacenado en la cache.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseModel> T getObject(Class<T> clazz, long id) {
        CacheNode node = nodes.get(new CacheKey(clazz, id));
        return node != null ? (T) node.getValue() : null;
    }

    /**
     * Devuelve un stream de objetos del tipo indicado, considerando enlaces desde un nodo raíz identificado
     * por (fromClass, fromId). El parámetro proxies indica clases que se deben usar como proxy para expandir la búsqueda.
     * El parámetro forward indica la dirección del enlace.
     */
    public <T extends BaseModel> Stream<T> getObjects(
            Class<? extends BaseModel> fromClass, long fromId,
            Class<T> clazz, Set<Class<? extends BaseModel>> proxies, boolean forward) {

        CacheNode rootNode = nodes.get(new CacheKey(fromClass, fromId));
        if (rootNode != null) {
            return getObjectStream(rootNode, clazz, proxies, forward);
        }
        return Stream.empty();
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseModel> Stream<T> getObjectStream(
            CacheNode rootNode, Class<T> clazz, Set<Class<? extends BaseModel>> proxies, boolean forward) {

        // Si la clase está en proxies, devolvemos un stream vacío.
        if (proxies.contains(clazz)) {
            return Stream.empty();
        }

        // Stream de nodos directos de la clase solicitada.
        Stream<T> directStream = rootNode.getLinks(clazz, forward).stream()
                .map(node -> (T) node.getValue());

        // Stream de nodos indirectos vía clases proxy.
        Stream<T> proxyStream = proxies.stream()
                .flatMap(proxyClass -> rootNode.getLinks(proxyClass, forward).stream()
                        .flatMap(node -> getObjectStream(node, clazz, proxies, forward)));

        return Stream.concat(directStream, proxyStream);
    }

    /**
     * Actualiza un objeto en la cache.
     */
    public void updateObject(BaseModel value) {
        CacheNode node = nodes.get(new CacheKey(value));
        if (node != null) {
            node.setValue(value);
        }
    }

    /**
     * Añade un enlace (link) bidireccional entre un objeto identificado por (fromClass, fromId)
     * y el objeto destino (toValue). Si el nodo destino no existe, se crea y se agrega a la cache.
     * Devuelve true si el nodo destino ya existía.
     */
    public boolean addLink(Class<? extends BaseModel> fromClass, long fromId, BaseModel toValue) {
        boolean existed = true;
        CacheNode fromNode = nodes.get(new CacheKey(fromClass, fromId));
        if (fromNode != null) {
            CacheKey toKey = new CacheKey(toValue);
            CacheNode toNode = nodes.get(toKey);
            if (toNode == null) {
                existed = false;
                toNode = new CacheNode(toValue);
                nodes.put(toKey, toNode);
            }
            fromNode.getLinks(toValue.getClass(), true).add(toNode);
            toNode.getLinks(fromClass, false).add(fromNode);
        }
        return existed;
    }

    /**
     * Elimina un enlace (link) entre el objeto de (fromClass, fromId) y el objeto de (toClass, toId).
     */
    public void removeLink(Class<? extends BaseModel> fromClass, long fromId, 
                           Class<? extends BaseModel> toClass, long toId) {
        CacheNode fromNode = nodes.get(new CacheKey(fromClass, fromId));
        if (fromNode != null) {
            CacheNode toNode = nodes.get(new CacheKey(toClass, toId));
            if (toNode != null) {
                fromNode.getLinks(toClass, true).remove(toNode);
                toNode.getLinks(fromClass, false).remove(fromNode);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CacheNode node : roots.values()) {
            printNode(sb, node, "");
        }
        return sb.toString().trim();
    }

    private void printNode(StringBuilder sb, CacheNode node, String indent) {
        sb.append('\n').append(indent).append(node.getValue().getClass().getSimpleName())
                .append('(').append(node.getValue().getId()).append(')');
        node.getAllLinks(true).forEach(child -> printNode(sb, child, indent + "  "));
    }
}