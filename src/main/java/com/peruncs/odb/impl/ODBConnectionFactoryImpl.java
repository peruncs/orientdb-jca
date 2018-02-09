package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.peruncs.odb.api.ODBConnectionFactory;
import com.peruncs.odb.api.ODBManagedConnectionFactory;

import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;


public class ODBConnectionFactoryImpl implements ODBConnectionFactory {

    private static final long serialVersionUID = 1L;

    private final ODBManagedConnectionFactory mcf;
    private final ConnectionManager cm;
    private Reference reference;
    
    ODBConnectionFactoryImpl(ODBManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }
    
    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    @Override
    public ODatabaseSession createSession() throws ResourceException {
        return (ODatabaseSession) cm.allocateConnection(mcf, null);
    }
}
