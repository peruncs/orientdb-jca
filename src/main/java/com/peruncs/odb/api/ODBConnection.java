package com.peruncs.odb.api;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.OrientDB;


import java.io.Closeable;



public interface ODBConnection extends Closeable {
    OrientDB getOrientDB();
    ODatabasePool getOrientDBPool();
    void close();
}
