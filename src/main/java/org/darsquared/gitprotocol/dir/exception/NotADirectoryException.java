package org.darsquared.gitprotocol.dir.exception;

import java.io.IOException;

public class NotADirectoryException extends IOException {

    public NotADirectoryException(String msg) {
        super(msg);
    }

}
