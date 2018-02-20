package org.darsquared.gitprotocol;

import java.util.Date;

public class Commit {

    private String digest;
    private byte[] data;
    private Date date;

    public Commit(String _digest, byte[] _data, Date _date) {
        this.digest = _digest;
        this.data = _data;
        this.date = _date;
    }

    public String getDigest() {
        return digest;
    }

    public byte[] getData() {
        return data;
    }

    public Date getDate() {
        return date;
    }
}
