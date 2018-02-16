package org.darsquared;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.darsquared.gitprotocol.GitProtocolImpl;
import org.darsquared.gitprotocol.storage.DHTStorage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class GitTest extends TestCase {

    private static final Logger logger = Logger.getLogger(GitTest.class.getName());

    private static final String DIRECTORY = "resources/";
    private static final String FAKE_REPO = "somefiles/";

    public GitTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(GitTest.class);
    }

    public void testMessageDigest() {
        GitProtocolImpl gp = null;
        try {
            gp = new GitProtocolImpl(new DHTStorage(1, 4001, "127.0.0.1", 4000));
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        File repo = new File(DIRECTORY + FAKE_REPO);
        logger.info("Creating repository");
        assertTrue(gp.createRepository("first_attempt", repo));
        File gitFolder = new File(DIRECTORY + GitProtocolImpl.GIT_FOLDER);
        logger.info("Checking if repository exists");
        assertTrue(gitFolder.exists());
        // TODO check if md is correct
        logger.info("Removing repository");
        assertTrue(gp.deleteRepo(repo));
    }
}
