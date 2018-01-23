package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.OConstants;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;


/**
 * @author Harald Wellmann
 *
 */
public class OrientManagedConnectionMetaData implements ManagedConnectionMetaData {
    
    @Override
    public String getEISProductName() {
        return "OrientDB";
    }

    @Override
    public String getEISProductVersion() {
        return OConstants.getVersion();
    }

    @Override
    public int getMaxConnections() {
        return 0;
    }

    @Override
    public String getUserName()  {
        return "admin";
    }

}
