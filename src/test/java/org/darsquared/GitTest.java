package org.darsquared;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.darsquared.gitprotocol.GitProtocolImpl;
import org.darsquared.gitprotocol.dir.Repository;
import org.darsquared.gitprotocol.storage.DHTStorage;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GitTest extends TestCase {

    private static final Logger log = Logger.getLogger(GitTest.class.getName());

    private static final String DIRECTORY = "resources/";
    private static final String FAKE_REPO = "somefiles/";
    private static final String FILENAME = "0";
    private static final String INITIAL_TEXT = "Some useless text";
    private static final String OTHER_TEXT = "Other useless text";
    private static final String REPO_NAME = "first_attempt";
    private static final String[] COMMITS = {Repository.FIRST_COMMIT_MESSAGE, "Second commit", "Third commit"};
    private static final File TEXT_FILE = new File(DIRECTORY + FAKE_REPO + FILENAME);


    public GitTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(GitTest.class);
    }


    public void setUp() throws Exception {
        super.setUp();
        log.info("Creating master node");
        writeSingleLine(TEXT_FILE, INITIAL_TEXT);
    }

    /**
     *  This test aims to check the correctness of hashing on files
     */
    public void testMessagesDigest() throws IOException {
        log.info("Test started. Good luck!");
        assertTrue(TEXT_FILE.exists());                         // Ok there is the file
        assertEquals(readSingleLine(TEXT_FILE), INITIAL_TEXT);  // just a warm up
        // Creating DHT and Gitprotocol
        GitProtocolImpl gp = null;
        try {
            gp = new GitProtocolImpl(new DHTStorage(0, 4000, "127.0.0.1", 4000));
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return;
        }
        File repo = new File(DIRECTORY + FAKE_REPO);
        log.info("Creating repository");
        assertTrue(gp.createRepository(REPO_NAME, repo));
        String firstCommitDigest = gp.getLastDigest();
        log.info("First commit done with hash: " + firstCommitDigest);
        // Now let's edit our file a little
        writeSingleLine(TEXT_FILE, OTHER_TEXT);
        assertEquals(OTHER_TEXT, readSingleLine(TEXT_FILE)); // ok, no jokes for us
        log.info("Trying to make a commit");
        log.info("Let's do our first commit");
        assertTrue(gp.commit(REPO_NAME, COMMITS[1]));
        String lastCommitDigest = gp.getLastDigest();
        log.info("Second commit done with hash: " + lastCommitDigest);
        assertFalse(firstCommitDigest.equals(lastCommitDigest));
        log.info("Let's do a void commit");
        assertFalse(gp.commit(REPO_NAME, COMMITS[2]));
        String voidCommit = gp.getLastDigest();
        log.info("Third commit done with hash: " + voidCommit);
        assertTrue(voidCommit.equals(lastCommitDigest));
        writeSingleLine(TEXT_FILE, INITIAL_TEXT);
        assertEquals(INITIAL_TEXT, readSingleLine(TEXT_FILE));
        assertTrue(gp.commit(REPO_NAME, COMMITS[2]));
        assertEquals(firstCommitDigest, gp.getLastDigest());
        List<String> commitMessages = gp.getCommits()
                .parallelStream()
                .map(c -> c.getMessage())
                .collect(Collectors.toList());
        commitMessages.forEach(log::info);  // all my commits
        log.info("Check if all commits are done");
        assertTrue(commitMessages.containsAll(Arrays.asList(COMMITS)));
    }

    @Override
    public void tearDown() throws IOException {
        log.info("Tear down");
        writeSingleLine(TEXT_FILE, INITIAL_TEXT);
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
