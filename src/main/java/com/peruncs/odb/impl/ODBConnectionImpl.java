package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.OrientDB;
import com.peruncs.odb.api.ODBConnection;

public class ODBConnectionImpl implements ODBConnection {

    private final ODBManagedConnectionImpl mc;

    ODBConnectionImpl(ODBManagedConnectionImpl mc) {
        this.mc = mc;
    }

    @Override
    public OrientDB getOrientDB(){
        return mc.getOrientDB();
    }

    @Override
    public ODatabasePool getOrientDBPool(){
        return mc.getOrientDBPool();
    }

    @Override
    public void close() {
        mc.close();
    }

}
