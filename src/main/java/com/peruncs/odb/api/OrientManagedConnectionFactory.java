package com.peruncs.odb.api;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;


/**
 * @author Harald Wellmann
 *
 */
public interface OrientManagedConnectionFactory extends ManagedConnectionFactory, ResourceAdapterAssociation, TransactionSupport {

}
