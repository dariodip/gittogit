package org.darsquared.gitprotocol.dir.exception;

import java.io.IOException;

/**
 * Thrown if we are trying to handle a file as it were a directory.
 */
public class NotADirectoryException extends IOException {

    public NotADirectoryException(String msg) {
        super(msg);
    }

}
