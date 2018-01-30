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
        displayName = "OrientDB",
        description = "Java JCA 1.7 adapter for OrientDB 3+. See https://github.com/peruncs/orientdb-jca ",
        eisType = "Database")

public class ODBResourceAdapter implements ResourceAdapter {

    private static final Log log = LogFactory.getLog(ODBResourceAdapter.class);

    public static final XAResource[] EMPTY_XA_RESOURCE = new XAResource[0];

    @ConfigProperty(description = "OrientDB Embedded Server Configuration")
    private String embeddedServerConfiguration;

    private OServer embeddedServer = null;
    private final String orientdbHome;

    public ODBResourceAdapter(){
        orientdbHome = new File("").getAbsolutePath(); //Set OrientDB home to current directory
        System.setProperty("ORIENTDB_HOME", orientdbHome);
    }

    public void setEmbeddedServerConfiguration(String embeddedServerConfiguration) {
        this.embeddedServerConfiguration = embeddedServerConfiguration;
    }

    public String getEmbeddedServerConfiguration() {
        return embeddedServerConfiguration;
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {

        log.info("Starting");

        if (embeddedServerConfiguration != null) try {
            embeddedServer = OServerMain.create(true);
            embeddedServer.startup(new File(embeddedServerConfiguration));
            embeddedServer.activate();
            logInfo("Successfully started");
        } catch (Exception e) {
            logError("Failed to start",e);
            throw new ResourceAdapterInternalException(e);
        }

    }

    @Override
    public void stop() {
        log.info("Stopping");
        if (embeddedServer != null && embeddedServer.isActive()) {
            try {
                embeddedServer.shutdown();
                logInfo("Successfully stopped");
            } catch (Exception e) {
                logError("Failed to stop",e);
            }
        }else{
            logInfo("Embedded server was not active or instantiated ");
        }
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

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

        ODBResourceAdapter other = (ODBResourceAdapter) obj;
        return Objects.equals(embeddedServerConfiguration, other.embeddedServerConfiguration);

    }

    private void logError(String message, Throwable e){
        log.error(message+" OrientDB embedded server, embedded server configuration: " + embeddedServerConfiguration + ", home: " + orientdbHome, e);
    }

    private void logInfo(String message){
        log.error(message+" OrientDB embedded server, embedded server configuration: " + embeddedServerConfiguration + ", home: " + orientdbHome);
    }

}
