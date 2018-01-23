package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.Orient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * @author Harald Wellmann
 * 
 */
// @formatter:off
@Connector(
    reauthenticationSupport = false, 
    transactionSupport = TransactionSupport.TransactionSupportLevel.LocalTransaction,
    version = "1.0",
    vendorName = "PerunCS",
    eisType = "OrientDB")
// @formatter:on
public class OrientResourceAdapter implements ResourceAdapter {

    private static Logger log = LoggerFactory.getLogger(OrientResourceAdapter.class);

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        log.debug("starting OrientResourceAdapter");

        // The VM running the app server may live longer than this adapter,
        // so we cannot use the default shutdown hook.
        // shutdown() is called directly in stop().

        Orient.instance().removeShutdownHook();
    }

    @Override
    public void stop() {
        log.debug("stopping OrientResourceAdapter");
        Orient.instance().shutdown();
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
        throws ResourceException {
        // not used
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        // not used
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
