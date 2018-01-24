package com.peruncs.odb.api;


public class OrientDatabaseConnectionInvalidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrientDatabaseConnectionInvalidException() {
        super("connection has already been closed");
    }
}
