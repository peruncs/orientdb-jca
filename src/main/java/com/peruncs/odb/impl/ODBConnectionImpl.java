package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.peruncs.odb.api.ODBConnection;

public class ODBConnectionImpl implements ODBConnection {

    private final ODBManagedConnectionImpl mc;

    ODBConnectionImpl(ODBManagedConnectionImpl mc) {
        this.mc = mc;
    }

    @Override
    public ODatabaseSession getSession() {
        return mc.getSession();
    }


    @Override
    public void close() {
        mc.close();
    }

}
