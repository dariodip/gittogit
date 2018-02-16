package org.darsquared.gitprotocol.storage;

public interface Storage<K, V> {

    boolean put(K key, V data);
    V get(K key);

}
