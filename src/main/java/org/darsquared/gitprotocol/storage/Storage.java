package org.darsquared.gitprotocol.storage;

import java.io.IOException;

/**
 * Storage interface.
 * @param <K> Key
 * @param <V> Value
 */
public interface Storage<K, V> {

    /**
     * Put an object ({@code data}) in the storage with a key ({@code key}).
     * @param key key of the data to store
     * @param data data to store
     * @return true if ok, false otherwise
     * @throws IOException thrown if something goes bad with the IO
     */
    boolean put(K key, V data) throws IOException;

    /**
     * Retrieve an object with key {@code key}
     * @param key key of the data to find
     * @return found data, null if not found
     * @throws ClassNotFoundException thrown if cast goes bad
     * @throws IOException thrown if IO goes bad
     */
    V get(K key) throws ClassNotFoundException, IOException;

}
