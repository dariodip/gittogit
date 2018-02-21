package org.darsquared.gitprotocol;

import org.darsquared.gitprotocol.dir.Repository;
import org.darsquared.gitprotocol.storage.DHTStorage;
import org.darsquared.gitprotocol.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

// TODO move zip/unzip in push and pull

public class GitProtocolImpl implements GitProtocol {

    private final Storage storage;
    private Repository repo;

    // dependency injection B-)
    public GitProtocolImpl(Storage _storage) {
        this.storage = _storage;
        this.repo = null;
    }

    public boolean createRepository(String _repo_name, File _directory) {
        if (this.repo != null) {
            return false;
        }
        try {
            this.repo = new Repository(_directory.getAbsolutePath(), _repo_name);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        return this.repo != null && this.repo.addFiles(files);
    }

    public boolean commit(String _repo_name, String _message) {
        if (this.repo == null) {
            return false;  // repo does not exist
        }
        try {
            this.repo.addCommit(_message, _repo_name);
        } catch (Exception e ){
            e.printStackTrace();
        }
        return true;
    }

    public String push(String _repo_name) {
        // TODO check conflicts
        if (this.repo == null) {
            return "";
        }
        try {
            ((DHTStorage)this.storage).put(_repo_name, this.repo);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        return ""; // TODO
    }

    public String pull(String _repo_name) {
        // TODO check conflicts
        try {
            this.repo = ((DHTStorage)storage).get(_repo_name);
            this.repo.replaceFiles();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getLastDigest() {
        return this.repo.getLastDigest();
    }

}
