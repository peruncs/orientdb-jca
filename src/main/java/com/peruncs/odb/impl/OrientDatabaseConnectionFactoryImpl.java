package com.peruncs.odb.impl;

import com.peruncs.odb.api.OrientDatabaseConnection;
import com.peruncs.odb.api.OrientDatabaseConnectionFactory;
import com.peruncs.odb.api.OrientManagedConnectionFactory;

import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;


public class OrientDatabaseConnectionFactoryImpl implements OrientDatabaseConnectionFactory {

    private static final long serialVersionUID = 1L;

    private OrientManagedConnectionFactory mcf;
    private ConnectionManager cm;
    private Reference reference;
    
    OrientDatabaseConnectionFactoryImpl(OrientManagedConnectionFactory mcf, ConnectionManager cm) {
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
    public OrientDatabaseConnection createConnection() throws ResourceException {
        return (OrientDatabaseConnection) cm.allocateConnection(mcf, null);
    }
}
