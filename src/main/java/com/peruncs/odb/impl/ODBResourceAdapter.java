package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.network.OServerNetworkListener;
import com.orientechnologies.orient.server.network.protocol.ONetworkProtocol;

import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


@Connector(
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction,
        version = "1.0",
        vendorName = "PerunCS",
        displayName = "OrientDB",
        description = "Java JCA 1.7 adapter for OrientDB 3+. See https://github.com/peruncs/orientdb-jca ",
        eisType = "Database")
public class ODBResourceAdapter implements ResourceAdapter {

    public static final XAResource[] EMPTY_XA_RESOURCE = new XAResource[0];
    private static final Logger log = Logger.getLogger(ODBResourceAdapter.class.getSimpleName());
    private final Map<String, ODatabasePool> pool = new HashMap<>();
    @ConfigProperty(description = "OrientDB Embedded Server Configuration")
    private String embeddedServerConfiguration;
    @ConfigProperty(description = "OrientDB Embedded Server Home")
    private String orientdbHome;
    private OServer embeddedServer = null;

    public ODBResourceAdapter() {
        setOrientdbHome(new File("").getAbsolutePath()); //Set default OrientDB home to current directory
    }

    public String getEmbeddedServerConfiguration() {
        return embeddedServerConfiguration;
    }

    public void setEmbeddedServerConfiguration(String embeddedServerConfiguration) {
        this.embeddedServerConfiguration = embeddedServerConfiguration;
    }

    public String getOrientdbHome() {
        return orientdbHome;
    }

    public void setOrientdbHome(String orientdbHome) {
        this.orientdbHome = orientdbHome;
        System.setProperty("ORIENTDB_HOME", orientdbHome);
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {

        log.info(() -> "ODB-JCA resource adapter starting ...");
        log.info(() -> "ODB-JCA server configuration: " + getEmbeddedServerConfiguration());
        log.info(() -> "ODB-JCA server home: " + getOrientdbHome());
        if (embeddedServerConfiguration != null)
            try {
                embeddedServer = OServerMain.create(true);
                embeddedServer.startup(new File(embeddedServerConfiguration));
                embeddedServer.activate();

                log.info(() -> "ODB-JCA database directory: " + embeddedServer.getDatabaseDirectory());
                log.info(() -> "ODB-JCA server id: " + embeddedServer.getServerId());
                log.info(() -> "ODB-JCA server activated: " + embeddedServer.isActive());

                Map<String, String> storageNames = embeddedServer.getAvailableStorageNames();
                if (storageNames != null) {
                    storageNames.forEach((String k, String v) -> log.info(() -> "ODB-JCA storage: " + k + ", " + v));
                }

                Map<String, Class<? extends ONetworkProtocol>> networkProtocols = embeddedServer.getNetworkProtocols();
                if (networkProtocols != null) {
                    networkProtocols.forEach((String k, Class<? extends ONetworkProtocol> v) -> {
                        log.info(() -> "ODB-JCA network protocol name: " + k + ", " + v.getSimpleName());
                        OServerNetworkListener l = embeddedServer.getListenerByProtocol(v);
                        log.info(() -> "ODB-JCA protocol listener inbound addr: " + l.getInboundAddr() + ", active:" + l.isActive() + ", alive:" + l.isAlive());
                    });
                }

                log.info(() -> "ODB-JCA resource adapter successfully started");

            } catch (Exception e) {
                log.log(Level.SEVERE, "ODB-JCA resource adapter failed to start", e);
                throw new ResourceAdapterInternalException(e);
            }

    }

    @Override
    public void stop() {
        log.info("ODB-JCA resource adapter stopping ...");

        pool.forEach((k, v) -> {
            v.close();
            log.info(()->"ODB-JCA closed database pool: " + k);
        });

        //if (embeddedServer != null && embeddedServer.isActive()) {
        if (embeddedServer != null) {
            embeddedServer.shutdown();
            log.info(()->"ODB-JCA embedded server stopped");
        }
        log.info("()->ODB-JCA resource adapter stopped");
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

    Map<String, ODatabasePool> getPool() {
        return pool;
    }
}
