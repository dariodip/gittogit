package org.darsquared.gitprotocol.storage;


import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.InetAddress;

public class DHTStorage implements Storage<String, Data> {

    final private PeerDHT peer;

    public DHTStorage(int peedId, int port, String bootstrapHostname, int bootstrapPort) throws IOException {
        peer = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(peedId)).ports(port).start()).start();

        FutureBootstrap bs = this.peer.peer().bootstrap().inetAddress(InetAddress.getByName(bootstrapHostname))
                .ports(bootstrapPort).start();
        bs.awaitUninterruptibly();
        if (bs.isSuccess()) {
            peer.peer().discover().peerAddress(bs.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    @Override
    public boolean put(String key, Data data) {
        peer.put(Number160.createHash(key)).data(data).start().awaitUninterruptibly();
        return true; // TODO
    }

    @Override
    public Data get(String key) {
        FutureGet futureGet = peer.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            return futureGet.dataMap().values().iterator().next();
        }
        return null;
    }
}
