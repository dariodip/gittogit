package org.darsquared;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.darsquared.gitprotocol.GitProtocol;
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
        assertTrue(gp.createRepository("first_attempt", repo));
        File gitFolder = new File(DIRECTORY + FAKE_REPO + GitProtocolImpl.GIT_FOLDER);
        logger.info("Checking if repository exists");
        assertTrue(gitFolder.exists());
        logger.info("Checking if git file exists");
        File gitFile = new File(DIRECTORY + FAKE_REPO + GitProtocolImpl.GIT_FOLDER + GitProtocolImpl.GIT_FILE);
        assertTrue(gitFile.exists());
        logger.info("Checking if git file has the correct length");
        assertEquals(countLinesInFile(gitFile), 1);

        // Now let's edit our file a little
        writeSingleLine(textFile, OTHER_TEXT);
        logger.info("Trying to make a commit");
        assertTrue(gp.commit("first_attempt", "I did it"));
        logger.info("Removing repository");
        // assertTrue(gp.deleteRepo(repo));
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
