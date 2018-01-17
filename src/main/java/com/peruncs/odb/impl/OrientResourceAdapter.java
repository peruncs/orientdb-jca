package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.Orient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import static javax.resource.spi.TransactionSupport.TransactionSupportLevel.LocalTransaction;


@Connector(
        transactionSupport = LocalTransaction,
        version = "1.0",
        vendorName = "PerunCS",
        eisType = "OrientDB")
public class OrientResourceAdapter implements ResourceAdapter {

    static final XAResource[] EMPTY_XA_RESOURCES = new XAResource[0];

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
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return EMPTY_XA_RESOURCES;
    }

}
