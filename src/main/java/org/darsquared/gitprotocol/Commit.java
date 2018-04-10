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

    @Override
    public String toString() {
        return String.format("[message:%s, repoName:%s, timestamp:%s, digest:%s]",
                getMessage(), getRepoName(), getTimestamp().toString(), getDigest());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Commit.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Commit other = (Commit) obj;
        return this.getDigest().equals(other.getDigest()) &&
                this.getRepoName().equals(other.getRepoName());
    }
}
