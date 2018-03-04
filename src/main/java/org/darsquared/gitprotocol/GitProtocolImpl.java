package org.darsquared.gitprotocol;

import org.darsquared.gitprotocol.dir.Repository;
import org.darsquared.gitprotocol.storage.DHTStorage;
import org.darsquared.gitprotocol.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class GitProtocolImpl implements GitProtocol {

    private final Storage storage;
    private Repository repo;

    /**
     * Implementation of {@link GitProtocol} interface.
     * @param _storage {@link Storage} object to store and query data.
     */
    public GitProtocolImpl(Storage _storage) {     // dependency injection B-)
        this.storage = _storage;
        this.repo = null;
    }

    /**
     * Create a repository for the directory ({@code _directory})
     * @param _repo_name a String, the name of the repository.
     * @param _directory a File, the directory where create the repository.
     * @return true if ok, false otherwise
     */
    public boolean createRepository(String _repo_name, File _directory) {
        if (this.repo != null) {
            return false;
        }
        try {
            this.repo = new Repository(_directory.getAbsolutePath(), _repo_name);
        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Adds new files to the repository. Newly added files are now tracked.
     * @param _repo_name a String, the name of the repository.
     * @param files a list of Files to be added to the repository.
     * @return true if ok, false otherwise
     */
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        try {
            return this.repo != null && this.repo.addFiles(files);
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Creates a commit.
     * @param _repo_name a String, the name of the repository.
     * @param _message a String, the message for this commit.
     * @return true if ok, false otherwise
     */

    public boolean commit(String _repo_name, String _message) {
        if (this.repo == null) {
            return false;  // repo does not exist
        }
        try {
            return this.repo.addCommit(_message, _repo_name);
        } catch (Exception e ){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pushes files on the storage.
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message
     */
    public String push(String _repo_name) {
        if (this.repo == null) {
            return Operationmessage.NO_REPO_FOUND;
        }
        try {
            Repository remoteRepo = ((DHTStorage)storage).get(_repo_name); // I am pulling the remote repo (HEAD)
            if (remoteRepo == null || this.repo.getDigests().contains(remoteRepo.getDigest())) { // my repo is correct
                ((DHTStorage)this.storage).put(_repo_name, this.repo);      // putting repo in DHT
                return Operationmessage.PUSH_SUCCESSFULL;                   // OK!
            } else {        // I am trying to push another branch
                return  Operationmessage.PULL_REQUIRED;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return Operationmessage.SOMETHING_GONE_WRONG + ": " + e.getMessage();
        }
    }

    /**
     * Pulls files from the storage.
     * @param _repo_name _repo_name a String, the name of the repository.
     * @return a String, operation message
     */
    public String pull(String _repo_name) {
        try {
            Repository pulledRepo = ((DHTStorage)storage).get(_repo_name);  // let's get remote repo
            if (pulledRepo == null) {                                       // no one
                return Operationmessage.NO_REPO_FOUND;                      // wth do you want to pull?
            }
            if(pulledRepo.getDigest().equals(this.repo.getDigest())) {      // you don't pushed new one
                return Operationmessage.NO_FILE_CHANGED;                    // :D
            }
            // if you have arrived here, everything is ok
            this.repo.replaceFilesFromMap(pulledRepo.getFilemap());         // replace files
            this.repo = pulledRepo;                                         // replace repository
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return Operationmessage.PULL_SUCCESSFULL;
    }

    public String getLastDigest() {
        return this.repo.getDigest();
    }

    public List<Commit> getCommits() {
        assert this.repo != null;
        return this.repo.getCommits();
    }

    public List<File> getFiles() {
        return this.repo.getFiles();
    }
}
