package com.peruncs.odb.api;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import java.io.Serializable;


public interface ODBConnectionFactory extends Serializable, Referenceable {
    ODBConnection createConnection() throws ResourceException;
}
