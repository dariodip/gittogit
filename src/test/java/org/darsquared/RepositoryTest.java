package org.darsquared;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.darsquared.gitprotocol.dir.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RepositoryTest extends TestCase {

    private static Logger logger = Logger.getLogger(RepositoryTest.class.getName());
    private static final String DIRECTORY = "resources/";
    private static final String FAKE_REPO = "somefiles/";

    public RepositoryTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(RepositoryTest.class);
    }

    public void testRepositoryClass() throws Exception {
        logger.info("Let's start!");
        logger.info("Creating repository");
        Repository repo = new Repository(DIRECTORY + FAKE_REPO, FAKE_REPO);
        assertNotNull(repo);
        logger.info("Repository successfully created.");
        assertEquals(1, repo.getFiles().size());
        assertNotNull(repo.getDigest());
        assertTrue(repo.getDigest().length() >= 1);
        logger.info("Repository has digest : " + repo.getDigest());
    }

    public void testDigest() throws Exception {
        logger.info("Creating first repository");
        Repository repo = new Repository(DIRECTORY + FAKE_REPO, FAKE_REPO);
        assertNotNull(repo);
        logger.info("First repository successfully created.");
        assertEquals(1, repo.getFiles().size());
        assertNotNull(repo.getDigest());
        assertTrue(repo.getDigest().length() >= 1);
        logger.info("First repository has digest : " + repo.getDigest());

        logger.info("Creating second repository");
        Repository repo2 = new Repository(DIRECTORY + FAKE_REPO, FAKE_REPO);
        assertNotNull(repo2);
        logger.info("Second repository successfully created.");
        assertEquals(1, repo2.getFiles().size());
        assertNotNull(repo2.getDigest());
        assertTrue(repo2.getDigest().length() >= 1);
        logger.info("Second repository has digest : " + repo2.getDigest());

        assertEquals(repo.getDigest(), repo2.getDigest());
        logger.info("Repos have the same digest.");
    }

    public void testFileList() throws Exception {
        logger.info("Let's start!");
        logger.info("Creating repository");
        Repository repo = new Repository(DIRECTORY + FAKE_REPO, FAKE_REPO);
        assertNotNull(repo);
        logger.info("Repository successfully created.");
        assertEquals(repo.getFiles().size(), 1);
        assertNotNull(repo.getDigest());
        assertTrue(repo.getDigest().length() >= 1);
        logger.info("Repository has digest : " + repo.getDigest());
        ArrayList<File> files = repo.getFiles();
        assertTrue(files.stream().allMatch(f -> f != null));
    }

}
