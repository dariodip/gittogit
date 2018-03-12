package org.darsquared.gitprotocol;


import java.io.Serializable;


public class Commit implements Serializable{

    private static final long serialVersionUID = 1L;
    private final String message;
    private final String repoName;
    private final Long timestamp;
    private final String digest;

    /**
     * A commit
     * @param message commit message
     * @param repoName name of the repository
     * @param digest
     */
    public Commit(String message, String repoName, String digest) {
        this.message = message;
        this.repoName = repoName;
        this.timestamp = System.currentTimeMillis();
        this.digest = digest;
    }

    /**
     * Returns commit message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns repo name
     */
    public String getRepoName() {
        return repoName;
    }

    /**
     * Returns commit timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns commit digest
     */
    public String getDigest() {
        return digest;
    }
}
