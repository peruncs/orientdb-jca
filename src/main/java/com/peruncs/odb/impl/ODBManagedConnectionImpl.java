package com.peruncs.odb.impl;


import com.orientechnologies.orient.core.OConstants;
import com.orientechnologies.orient.core.db.ODatabaseSession;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static javax.resource.spi.ConnectionEvent.CONNECTION_CLOSED;

class ODBManagedConnectionImpl implements ManagedConnection, Closeable {

    private final ODBManagedConnectionFactoryImpl mcf;
    private final List<ConnectionEventListener> listeners = new ArrayList<>();
    private final ConnectionRequestInfo cri;
    private PrintWriter logWriter = new PrintWriter(System.out);

    public ODBManagedConnectionImpl(ODBManagedConnectionFactoryImpl mcf, ConnectionRequestInfo cri) {
        this.mcf = mcf;
        this.cri = cri;
    }

    @Override
    public void destroy() {
    }

    @Override
    public ODatabaseSession getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException{
        return mcf.newSession();
    }


    @Override
    public void cleanup() {
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new ResourceException("OrientDB resource adapter does not support local transactions");

    }

    @Override
    public void associateConnection(Object connection) {

    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {

        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {

        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new ResourceException("OrientDB resource adapter does not support XA transactions");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() {

        return new ManagedConnectionMetaData() {
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
                return mcf.getMaxPoolSize();
            }

            @Override
            public String getUserName() {
                return "admin";
            }

        };
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public void close() {
        fireConnectionEvent(CONNECTION_CLOSED);
    }

    private void fireConnectionEvent(int event) {

        ConnectionEvent connectionEvent = new ConnectionEvent(this, event);
        synchronized (listeners) {
            for (ConnectionEventListener listener : this.listeners) {
                switch (event) {
                    case CONNECTION_CLOSED:
                        listener.connectionClosed(connectionEvent);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown event: " + event);
                }
            }
        }
    }

    public ConnectionRequestInfo getConnectionRequestInfo() {
        return cri;
    }


}
