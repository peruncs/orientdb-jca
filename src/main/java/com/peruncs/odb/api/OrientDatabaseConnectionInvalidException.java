package com.peruncs.odb.api;

/**
 * @author Markus Menner
 */
public class OrientDatabaseConnectionInvalidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrientDatabaseConnectionInvalidException() {
        super("connection has already been closed");
    }
}
