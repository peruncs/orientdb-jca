package com.peruncs.odb.api;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;


public interface ODBManagedConnectionFactory extends ManagedConnectionFactory, ResourceAdapterAssociation, TransactionSupport {

}