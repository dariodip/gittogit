package org.darsquared.gitprotocol.storage;


import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.darsquared.gitprotocol.dir.Repository;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;

/**
 * A storage using DHT.
 */
public class DHTStorage implements Storage<String, Repository> {

    final private PeerDHT peer;

    /**
     * Connect a DHT and use it as storage
     * @param peedId identifier of the peer
     * @param port peer port
     * @param bootstrapHostname hostname of the peer for the bootstrap
     * @param bootstrapPort port of the peer for the bootstrap
     * @throws IOException thrown if IO goes bad
     */
    public DHTStorage(int peedId, int port, String bootstrapHostname, int bootstrapPort) throws IOException {
        peer = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(peedId)).ports(port).start()).start();

        FutureBootstrap bs = this.peer.peer().bootstrap().inetAddress(InetAddress.getByName(bootstrapHostname))
                .ports(bootstrapPort).start();
        bs.awaitUninterruptibly();
        if (bs.isSuccess()) {
            peer.peer().discover().peerAddress(bs.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    /**
     * Put a {@link Repository} ({@code data}) in the DHT with a key ({@code key}).
     * @param key ({@link String)} key of the data to store
     * @param data data to store
     * @return true if ok, false otherwise
     * @throws IOException thrown if something goes bad with the IO
     */
    @Override
    public boolean put(String key, Repository data) throws IOException {
        peer.put(Number160.createHash(key)).data(new Data(data)).start().awaitUninterruptibly();
        return true; // TODO
    }


    /**
     * Retrieve a {@link Repository} with key {@code key}
     * @param key key of the data to find
     * @return found data, null if not found
     * @throws ClassNotFoundException thrown if cast goes bad
     * @throws IOException thrown if IO goes bad
     */
    @Override
    public Repository get(String key) throws ClassNotFoundException, IOException {
        FutureGet futureGet = peer.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            Collection<Data> dataMapValues = futureGet.dataMap().values();
            if (dataMapValues.isEmpty()) {
                return null;
            }
            return (Repository) futureGet.dataMap().values().iterator().next().object();
        }
        return null;
    }
}
