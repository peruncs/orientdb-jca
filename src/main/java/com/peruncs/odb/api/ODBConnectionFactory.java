package com.peruncs.odb.api;

import com.orientechnologies.orient.core.db.ODatabaseSession;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import java.io.Serializable;


public interface ODBConnectionFactory extends Serializable, Referenceable {
    ODatabaseSession createSession() throws ResourceException;
}
