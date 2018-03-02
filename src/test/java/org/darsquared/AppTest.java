package org.darsquared;

import junit.framework.TestCase;
import org.darsquared.gitprotocol.GitProtocol;
import org.darsquared.gitprotocol.GitProtocolImpl;
import org.darsquared.gitprotocol.Operationmessage;
import org.darsquared.gitprotocol.storage.DHTStorage;

import java.io.*;
import java.util.logging.Logger;

public class AppTest extends TestCase {

    private DHTStorage storage;
    private GitProtocol gitProtocol;
    private final static String INITIAL_STRING = "Initial string";
    private final static String SECOND_STRING = "Second string";
    private final static String NOT_FOUND = "not found";
    private final static String BOOTSTRAP_HN = "127.0.0.1";
    private final static Logger log = Logger.getLogger(DHTTest.class.getName());
    private final static Integer MASTER_PEER_ID = 0;
    private final static Integer PEER_ID = 1;
    private final static String REPO_NAME = "A_FILE";
    private static final String DIRECTORY = "resources/";
    private static final String FAKE_REPO = "somefiles/";
    private static final File REPO = new File(DIRECTORY + FAKE_REPO);
    private static final File REPO_FILE = new File(DIRECTORY + FAKE_REPO + "0");


    public void setUp() throws Exception {
        super.setUp();
        log.info("Creating master node");
        storage = new DHTStorage(MASTER_PEER_ID, 4000, BOOTSTRAP_HN, 4000);
        gitProtocol = new GitProtocolImpl(storage);
        writeSingleLine(REPO_FILE, INITIAL_STRING);
    }

    public void testStorage() throws Exception {
        assertNotNull(storage);
        assertNull(storage.get("Nothing"));
    }

    public void testGit() throws Exception {
        assertNotNull(storage);
        assertNotNull(gitProtocol);
        assertEquals(readSingleLine(REPO_FILE), INITIAL_STRING);
        log.info("Creating first commit");
        assertTrue(gitProtocol.createRepository(REPO_NAME, REPO));
        log.info("Making first commit");
        assertTrue(gitProtocol.commit(REPO_NAME, "Initial commit"));
        log.info("Trying to make a pull: it should not work");
        assertEquals(gitProtocol.pull(REPO_NAME), Operationmessage.NO_REPO_FOUND);
        log.info("Making first push");
        assertEquals(gitProtocol.push(REPO_NAME), Operationmessage.PUSH_SUCCESSFULL);
        log.info("Pulling repo");
        assertEquals(gitProtocol.pull(REPO_NAME), Operationmessage.NO_FILE_CHANGED);
        log.info("Now let's edit the file a little");
        writeSingleLine(REPO_FILE, SECOND_STRING);
        log.info("Commit and pull");
        assertTrue(gitProtocol.commit(REPO_NAME, "A little edit"));
        assertEquals(gitProtocol.pull(REPO_NAME), Operationmessage.PULL_SUCCESSFULL);
        assertEquals(readSingleLine(REPO_FILE), INITIAL_STRING);

    }

    public void tearDown() throws Exception {
        super.tearDown();
        writeSingleLine(REPO_FILE, INITIAL_STRING);
    }

    /*********************************
     *********************************
     *  UTILITY METHODS              *
     *********************************
     *********************************/

    private int countLinesInFile(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        int count = 0;
        while(br.readLine() != null) count++;
        br.close();
        return count;
    }

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