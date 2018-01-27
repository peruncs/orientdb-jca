package com.peruncs.odb.impl;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.io.File;
import java.util.Objects;


@Connector(
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction,
    version = "1.0",
    vendorName = "PerunCS",
    eisType = "OrientDB")

public class OrientResourceAdapter implements ResourceAdapter {

    private static final Log log = LogFactory.getLog(OrientResourceAdapter.class);

    public static final XAResource[] EMPTY_XA_RESOURCE = new XAResource[0];

    @ConfigProperty(description="OrientDB Embedded Server Configuration")
    private String embeddedServerConfiguration;

    private OServer embeddedServer = null;

    public void setEmbeddedServerConfiguration(String embeddedServerConfiguration) {
        this.embeddedServerConfiguration = embeddedServerConfiguration;
    }

    public String getEmbeddedServerConfiguration() {
        return embeddedServerConfiguration;
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        log.info("Starting OrientResourceAdapter, embedded server configuration: "+embeddedServerConfiguration);

        if(embeddedServerConfiguration!=null) try {
            embeddedServer = OServerMain.create(true);
            embeddedServer.startup(new File(embeddedServerConfiguration));
            embeddedServer.activate();
        } catch (Exception e) {
            throw new ResourceAdapterInternalException(e);
        }

        // The VM running the app server may live longer than this adapter,
        // so we cannot use the default shutdown hook.
        // shutdown() is called directly in stop().
        // Orient.instance().removeShutdownHook();
    }

    @Override
    public void stop() {
        log.info("Stopping OrientResourceAdapter");
        if(embeddedServer !=null && embeddedServer.isActive()){
            embeddedServer.shutdown();
            log.info("Shut down server "+embeddedServerConfiguration);
        }
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec){}

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec){}

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) {
        return EMPTY_XA_RESOURCE;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(embeddedServerConfiguration);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        OrientResourceAdapter other = (OrientResourceAdapter) obj;
        return Objects.equals(embeddedServerConfiguration,other.embeddedServerConfiguration);

    }
}
