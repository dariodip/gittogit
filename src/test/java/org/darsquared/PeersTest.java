package org.darsquared;

import jline.internal.Log;
import junit.framework.TestCase;
import org.darsquared.gitprotocol.GitProtocol;
import org.darsquared.gitprotocol.GitProtocolImpl;
import org.darsquared.gitprotocol.Operationmessage;
import org.darsquared.gitprotocol.dir.Repository;
import org.darsquared.gitprotocol.storage.DHTStorage;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PeersTest extends TestCase {

    private DHTStorage storageMaster;
    private DHTStorage storage1;
    private DHTStorage storage2;
    private DHTStorage storage3;

    private GitProtocolImpl gitProtocolMaster;
    private GitProtocolImpl gitProtocol1;
    private GitProtocolImpl gitProtocol2;
    private GitProtocolImpl gitProtocol3;
    private final static String INITIAL_STRING = "Lorem ipsum dolor sit amet";
    private final static String SECOND_STRING = "Consectetur adipiscing elit";
    private final static String SEC_INITIAL_STRING = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua";
    private final static String SEC_SECOND_STRING = "Ut enim ad minim veniam";
    private final static String BOOTSTRAP_HN = "127.0.0.1";
    private final static Logger log = Logger.getLogger(PeersTest.class.getName());
    private final static Integer MASTER_PEER_ID = 0;
    private final static Integer PEER_ID_1 = 1;
    private final static Integer PEER_ID_2 = 2;
    private final static Integer PEER_ID_3 = 3;
    private final static String REPO_NAME = "A_FILE";
    private static final String DIRECTORY = "resources/";
    private static final String REPO_MASTER_PEER = "masterPeer/";
    private static final String REPO_PEER_1 = "peer1/";
    private static final String REPO_PEER_2 = "peer2/";
    private static final String REPO_PEER_3 = "peer3/";
    private static final File REPO1 = new File(DIRECTORY + REPO_PEER_1);
    private static final File REPO1_FILE = new File(DIRECTORY + REPO_PEER_1 + "file");
    private static final File REPO2 = new File(DIRECTORY + REPO_PEER_2);
    private static final File REPO2_FILE = new File(DIRECTORY + REPO_PEER_2 + "file");
    private static final File REPO3 = new File(DIRECTORY + REPO_PEER_3);
    private static final File REPO3_FILE = new File(DIRECTORY + REPO_PEER_3 + "file");
    private static final File MASTER_REPO = new File(DIRECTORY + REPO_MASTER_PEER);
    private static final File MASTER_REPO_FILE = new File(DIRECTORY + REPO_MASTER_PEER + "0");
    private static final File SEC_MASTER_REPO_FILE = new File(DIRECTORY + REPO_MASTER_PEER + "1");

    private void freeRepo() throws IOException {
        File[] files = MASTER_REPO.listFiles();
        if(files!=null) {
            for (File f : files) {
                assertTrue(Files.deleteIfExists(f.toPath()));
            }
        }
        files = REPO1.listFiles();
        if(files!=null) {
            for (File f : files) {
                assertTrue(f.delete());
            }
        }
        files = REPO2.listFiles();
        if(files!=null) {
            for (File f : files) {
                assertTrue(f.delete());
            }
        }
        files = REPO3.listFiles();
        if(files!=null) {
            for (File f : files) {
                assertTrue(f.delete());
            }
        }
    }

    public void setUp() throws Exception {
        super.setUp();
        log.info("Free all repository");
        freeRepo();
        log.info("Creating master node");
        storageMaster = new DHTStorage(MASTER_PEER_ID, 4000, BOOTSTRAP_HN, 4000);
        gitProtocolMaster = new GitProtocolImpl(storageMaster);
        log.info("Creating node 1");
        storage1 = new DHTStorage(PEER_ID_1, 4000, BOOTSTRAP_HN, 4000);
        gitProtocol1 = new GitProtocolImpl(storage1);
        log.info("Creating node 2");
        storage2 = new DHTStorage(PEER_ID_2, 4000, BOOTSTRAP_HN, 4000);
        gitProtocol2 = new GitProtocolImpl(storage2);
        log.info("Creating node 3");
        storage3 = new DHTStorage(PEER_ID_3, 4000, BOOTSTRAP_HN, 4000);
        gitProtocol3 = new GitProtocolImpl(storage3);
    }

    public void testStorage() throws Exception {
        assertNotNull(storageMaster);
        assertNull(storageMaster.get("Nothing"));
        assertNotNull(storage1);
        assertNull(storage1.get("Nothing"));
        assertNotNull(storage2);
        assertNull(storage2.get("Nothing"));
        assertNotNull(storage3);
        assertNull(storage3.get("Nothing"));
    }

    public void testCommitPullPush() throws Exception {
        assertNotNull(storageMaster);                                 // storage not null
        assertNotNull(storage1);                                 // storage not null
        assertNotNull(storage2);                                 // storage not null
        assertNotNull(storage3);                                 // storage not null

        assertNotNull(gitProtocolMaster);                             // gitprotocol class not null
        assertNotNull(gitProtocol1);                             // gitprotocol class not null
        assertNotNull(gitProtocol2);                             // gitprotocol class not null
        assertNotNull(gitProtocol3);                             // gitprotocol class not null

        //All Peers create repository
        assertTrue(gitProtocolMaster.createRepository(REPO_NAME+"master",MASTER_REPO));
        assertTrue(gitProtocol1.createRepository(REPO_NAME+"1",REPO1));
        assertTrue(gitProtocol2.createRepository(REPO_NAME+"2",REPO2));
        assertTrue(gitProtocol3.createRepository(REPO_NAME+"3",REPO3));

        //Master Peer create a File, add to git, commit and push
        log.info("Master peer: create a file");
        Path aFile = Files.createFile(Paths.get(MASTER_REPO_FILE.toURI()));
        Files.write(aFile,INITIAL_STRING.getBytes());
        log.info("Master peer: creation complete");
        List<File> toAdd = new ArrayList<>();
        toAdd.add(aFile.toFile());
        log.info("Master peer: add");
        gitProtocolMaster.addFilesToRepository(REPO_NAME+"master", toAdd);
        log.info("Master peer: commit");
        assertTrue(gitProtocolMaster.commit(REPO_NAME+"master",Repository.FIRST_COMMIT_MESSAGE));
        log.info("Master peer: push");
        assertEquals(Operationmessage.PUSH_SUCCESSFULL,gitProtocolMaster.push(REPO_NAME+"master"));

        //First Peer create a File, add to git, commit and push
        log.info("First peer: create a file");
        aFile = Files.createFile(Paths.get(REPO1_FILE.toURI()));
        Files.write(aFile,INITIAL_STRING.getBytes());
        log.info("First peer: creation complete");
        toAdd = new ArrayList<>();
        toAdd.add(aFile.toFile());
        log.info("First peer: add");
        gitProtocol1.addFilesToRepository(REPO_NAME+"1", toAdd);
        log.info("First peer: commit");
        assertTrue(gitProtocol1.commit(REPO_NAME+"1",Repository.FIRST_COMMIT_MESSAGE));
        log.info("First peer: push");
        assertEquals(Operationmessage.PUSH_SUCCESSFULL,gitProtocol1.push(REPO_NAME+"1"));

        //Second Peer create a File, add to git, commit and push
        log.info("Second peer: create a file");
        aFile = Files.createFile(Paths.get(REPO2_FILE.toURI()));
        Files.write(aFile,INITIAL_STRING.getBytes());
        log.info("Second peer: creation complete");
        toAdd = new ArrayList<>();
        toAdd.add(aFile.toFile());
        log.info("Second peer: add");
        gitProtocol2.addFilesToRepository(REPO_NAME+"2", toAdd);
        log.info("Second peer: commit");
        assertTrue(gitProtocol2.commit(REPO_NAME+"2",Repository.FIRST_COMMIT_MESSAGE));
        log.info("Second peer: push");
        assertEquals(Operationmessage.PUSH_SUCCESSFULL,gitProtocol2.push(REPO_NAME+"2"));

        //Third Peer create a File, add to git, commit and push
        log.info("Third peer: create a file");
        aFile = Files.createFile(Paths.get(REPO3_FILE.toURI()));
        Files.write(aFile,INITIAL_STRING.getBytes());
        log.info("Third peer: creation complete");
        toAdd = new ArrayList<>();
        toAdd.add(aFile.toFile());
        log.info("Third peer: add");
        gitProtocol3.addFilesToRepository(REPO_NAME+"3", toAdd);
        log.info("Third peer: commit");
        assertTrue(gitProtocol3.commit(REPO_NAME+"3",Repository.FIRST_COMMIT_MESSAGE));
        log.info("Third peer: push");
        assertEquals(Operationmessage.PUSH_SUCCESSFULL,gitProtocol3.push(REPO_NAME+"3"));

        //TODO Valutare se rimuovere il codice commentato

//        //Peer 1, 2, 3 pull the repo
//        log.info("Peer 1: pull");
//        assertEquals(Operationmessage.PULL_SUCCESSFULL, gitProtocol1.pull(REPO_NAME));
//        log.info("Peer 2: pull");
//        assertEquals(Operationmessage.PULL_SUCCESSFULL, gitProtocol2.pull(REPO_NAME));
//        log.info("Peer 3: pull");
//        assertEquals(Operationmessage.PULL_SUCCESSFULL, gitProtocol3.pull(REPO_NAME));
//
//        //Peer 1 edit the file, commit and push
//        log.info("Peer 1: editing file");
//        Files.write( new File(DIRECTORY + REPO_PEER_1 + "0").toPath(), ("\n"+SECOND_STRING).getBytes(), StandardOpenOption.APPEND);
//        log.info("Peer 1: commit");
//        assertTrue(gitProtocol1.commit(REPO_NAME, "File edited"));
//        log.info("Peer 1: push");
//        assertEquals(Operationmessage.PUSH_SUCCESSFULL, gitProtocol1.push(REPO_NAME));
//        log.info("Master peer: pull");
//        assertEquals(Operationmessage.PULL_SUCCESSFULL, gitProtocolMaster.pull(REPO_NAME));
//        log.info("Peer 2: pull");
//        assertEquals(Operationmessage.PULL_SUCCESSFULL, gitProtocol2.pull(REPO_NAME));
//        log.info("Peer 3: pull");
//        assertEquals(Operationmessage.PULL_SUCCESSFULL, gitProtocol3.pull(REPO_NAME));
//
//        assertEquals(2,Files.readAllLines(gitProtocolMaster.getFiles().get(0).toPath()).size());
//        assertEquals(2,Files.readAllLines(gitProtocol1.getFiles().get(0).toPath()).size());
//        assertEquals(2,Files.readAllLines(gitProtocol2.getFiles().get(0).toPath()).size());
//        assertEquals(2,Files.readAllLines(gitProtocol3.getFiles().get(0).toPath()).size());

//        assertEquals(readSingleLine(REPO_FILE), INITIAL_STRING);    // file gets correct text
//        log.info("Creating first commit");
//        assertTrue(gitProtocol.createRepository(REPO_NAME, REPO));  // repo creation ok
//        assertEquals(1, gitProtocol.getCommits().size());
//        log.info("Creating again repo");
//        assertFalse(gitProtocol.createRepository(REPO_NAME, REPO)); // cannot create again repo
//        log.info("Trying to make a pull: it should not work");
//        assertEquals(gitProtocol.pull(REPO_NAME), Operationmessage.NO_REPO_FOUND);  // i'm trying to pull a repo not in dht
//        log.info("Making first push");
//        assertEquals(gitProtocol.push(REPO_NAME), Operationmessage.PUSH_SUCCESSFULL); // pushing repo
//        log.info("Pulling repo");
//        assertEquals(gitProtocol.pull(REPO_NAME), Operationmessage.NO_FILE_CHANGED); // pull repo (no changes)
//        log.info("Now let's edit the file a little");
//
//        writeSingleLine(REPO_FILE, SECOND_STRING);  // write a little edit in file
//        assertEquals(readSingleLine(REPO_FILE), SECOND_STRING);  // was it written?
//        log.info("Commit and pull");
//        assertTrue(gitProtocol.commit(REPO_NAME, "A little edit")); // new commit
//        assertEquals(readSingleLine(REPO_FILE), SECOND_STRING);  // unchanged
//        assertTrue(gitProtocol.getCommits().size() == 2);
//        log.info("Pulling repo: it should delete last commit");
//        assertEquals(gitProtocol.pull(REPO_NAME), Operationmessage.PULL_SUCCESSFULL);
//        assertEquals(readSingleLine(REPO_FILE), INITIAL_STRING);
//        assertEquals(1, gitProtocol.getCommits().size());
//        assertEquals(1, gitProtocol.getFiles().size());
//
//        // Let's put our hands on the second file
//        log.info("Writing second file");
//        writeSingleLine(SEC_REPO_FILE, SEC_INITIAL_STRING);
//        assertEquals(SEC_INITIAL_STRING, readSingleLine(SEC_REPO_FILE));
//        log.info("Now I try to make a commit (it should not work)");
//        assertFalse(gitProtocol.commit(REPO_NAME, "Not a valid commit, second file is not traked"));
//        assertEquals(1, gitProtocol.getCommits().size());
//        log.info("Let's add second file to repo");
//        assertTrue(gitProtocol.addFilesToRepository(REPO_NAME, Arrays.asList(SEC_REPO_FILE)));
//        log.info("Let's do another commit");
//        assertTrue(gitProtocol.commit(REPO_NAME, "Now I'll commit with new file"));
//        log.info("Commit done");
//        assertEquals(2, gitProtocol.getFiles().size());
//        assertEquals(2, gitProtocol.getCommits().size());
    }

    public void tearDown() throws Exception {
        super.tearDown();
//        writeSingleLine(REPO_FILE, INITIAL_STRING);
//        SEC_REPO_FILE.delete();
    }

    /*********************************
     *********************************
     *  UTILITY METHODS              *
     *********************************
     *********************************/

    private String readSingleLine(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String toRet = br.readLine();
        br.close();
        return toRet;
    }

    private void writeSingleLine(File f, String line) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(f));
        wr.write(line);
        wr.close();
    }

}