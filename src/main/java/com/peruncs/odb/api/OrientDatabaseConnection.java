package com.peruncs.odb.api;

import com.orientechnologies.orient.core.db.OrientDB;


import java.io.Closeable;


/**
 * @author Harald Wellmann
 *
 */
public interface OrientDatabaseConnection extends Closeable {
    OrientDB document();
    void close();
}
