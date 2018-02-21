package org.darsquared.gitprotocol;


import java.io.Serializable;

public class Commit implements Serializable{

    private static final long serialVersionUID = 1L;
    private final String message;
    private final String repoName;
    private final Long timestamp;
    private final String digest;

    public Commit(String message, String repoName, String digest) {
        this.message = message;
        this.repoName = repoName;
        this.timestamp = System.currentTimeMillis();
        this.digest = digest;
    }

    public String getMessage() {
        return message;
    }

    public String getRepoName() {
        return repoName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getDigest() {
        return digest;
    }
}
