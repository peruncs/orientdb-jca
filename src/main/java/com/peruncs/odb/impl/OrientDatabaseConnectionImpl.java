package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.OrientDB;
import com.peruncs.odb.api.OrientDatabaseConnection;
import com.peruncs.odb.api.OrientDatabaseConnectionInvalidException;


/**
 * @author Harald Wellmann
 * 
 */
public class OrientDatabaseConnectionImpl implements OrientDatabaseConnection {

    private OrientManagedConnectionImpl mc;
    private ODatabaseInternal<?> db;
    private boolean valid = true;

    OrientDatabaseConnectionImpl(ODatabaseInternal<?> db, OrientManagedConnectionImpl mc) {
        this.db = db;
        this.mc = mc;
    }


    @Override
    public OrientDB document(){
        return null;
    }



    @Override
    public void close() {
        mc.close();
    }

    protected synchronized void setValid(boolean valid) {
        this.valid = valid;
    }

    protected synchronized boolean isValid() {
        return valid;
    }

    private void checkValidity() {
        if (!isValid()) {
            throw new OrientDatabaseConnectionInvalidException();
        }
    }
}
