package com.peruncs.odb.api;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import java.io.Serializable;


public interface OrientDatabaseConnectionFactory extends Serializable, Referenceable {
    OrientDatabaseConnection createConnection() throws ResourceException;
}
