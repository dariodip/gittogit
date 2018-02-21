package org.darsquared;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.darsquared.gitprotocol.GitProtocolImpl;
import org.darsquared.gitprotocol.storage.DHTStorage;

import java.io.*;
import java.util.logging.Logger;

public class GitTest extends TestCase {

    private static final Logger logger = Logger.getLogger(GitTest.class.getName());

    private static final String DIRECTORY = "resources/";
    private static final String FAKE_REPO = "somefiles/";
    private static final String FILENAME = "0";
    private static final String INITIAL_TEXT = "Some useless text";
    private static final String OTHER_TEXT = "Other useless text";
    public static final String REPO_NAME = "first_attempt";

    public GitTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(GitTest.class);
    }

    public void testMessageDigest() throws IOException {
        logger.info("Test started. Good luck!");
        File textFile = new File(DIRECTORY + FAKE_REPO + FILENAME);
        assertTrue(textFile.exists());
        assertEquals(readSingleLine(textFile), INITIAL_TEXT);  // just a warm up
        GitProtocolImpl gp = null;
        try {
            gp = new GitProtocolImpl(new DHTStorage(0, 4000, "127.0.0.1", 4000));
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return;
        }
        File repo = new File(DIRECTORY + FAKE_REPO);
        logger.info("Creating repository");
        assertTrue(gp.createRepository(REPO_NAME, repo));
        logger.info("Let's do our first commit");
        assertTrue(gp.commit("first_attempt", "First commit"));
        String firstCommitDigest = gp.getLastDigest();
        logger.info("First commit done with hash: " + firstCommitDigest);
        // Now let's edit our file a little
        writeSingleLine(textFile, OTHER_TEXT);
        logger.info("Trying to make a commit");
        assertTrue(gp.commit("first_attempt", "I did it"));
        String lastCommitDigest = gp.getLastDigest();
        logger.info("Second commit done with hash: " + lastCommitDigest);
        assertFalse(firstCommitDigest.equals(lastCommitDigest));
        logger.info("Let's do a void commit");
        gp.commit(REPO_NAME, "No changes");
        String voidCommit = gp.getLastDigest();
        logger.info("Third commit done with hash: " + voidCommit);
        assertTrue(voidCommit.equals(lastCommitDigest));
    }

    @Override
    public void tearDown() throws IOException {
        logger.info("Tear down");
        File textFile = new File(DIRECTORY + FAKE_REPO + FILENAME);
        writeSingleLine(textFile, INITIAL_TEXT);
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
