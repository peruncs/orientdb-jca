package com.peruncs.odb.api;

import com.orientechnologies.orient.core.db.ODatabaseSession;

import java.io.Closeable;



public interface ODBConnection extends Closeable {
    ODatabaseSession getSession();
    void close();
}
