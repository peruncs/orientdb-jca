package com.peruncs.odb.api;

import com.orientechnologies.orient.core.db.ODatabaseSession;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;

public interface ODBManagedConnectionFactory extends ManagedConnectionFactory, ResourceAdapterAssociation {
     ODatabaseSession newSession() throws ResourceException ;
}
