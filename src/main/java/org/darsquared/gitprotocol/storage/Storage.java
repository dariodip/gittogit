package org.darsquared.gitprotocol.storage;

import java.io.IOException;

public interface Storage<K, V> {

    boolean put(K key, V data) throws IOException;
    V get(K key) throws ClassNotFoundException, IOException;

}
