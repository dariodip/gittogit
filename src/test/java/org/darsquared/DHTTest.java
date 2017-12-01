package org.darsquared;

import junit.framework.TestCase;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DHTTest {

    final private PeerDHT peerDHT;
    final private static String NOT_FOUND = "not found";
    final private static Logger log = Logger.getLogger(DHTTest.class.getName());


    private DHTTest(Integer peerId) throws IOException {
        log.info("Trying to add me as a peer");
        peerDHT = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(peerId))
                .ports(4000 + peerId).start())
                .start();
        log.info("Finding master peer");
        FutureBootstrap fb = this.peerDHT.peer()
                .bootstrap()
                .inetAddress(InetAddress.getByName("127.0.0.1"))
                .ports(4001) // Contact master peer
                .start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peerDHT.peer()
                    .discover()
                    .peerAddress(fb.bootstrapTo().iterator().next())
                    .start()
                    .awaitUninterruptibly();
            log.info("Connection successful completed");
        }
    }

    private void store(String name, String ip) throws IOException {
        log.info("Storing value " + ip + " in key: " + name);
        peerDHT
                .put(Number160.createHash(name))
                .data(new Data(ip))
                .start()
                .awaitUninterruptibly();
    }

    private String get(String name) throws IOException, ClassNotFoundException {
        log.info("Getting value for key " + name);
        FutureGet futureGet = peerDHT.get(Number160.createHash(name)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            return futureGet
                    .dataMap()
                    .values()
                    .iterator()
                    .next()
                    .object()
                    .toString();
        }
        return NOT_FOUND;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        log.info("Starting test");
        DHTTest dns = new DHTTest(Integer.parseInt(args[0]));
        if (args.length == 3) {
            dns.store(args[1], args[2]);
        }
        if (args.length == 2) {
            System.out.println("Name:" + args[1] + " IP:" + dns.get(args[1]));
        }
    }

}
