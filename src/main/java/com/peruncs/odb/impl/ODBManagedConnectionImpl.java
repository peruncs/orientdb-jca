package com.peruncs.odb.impl;


import com.orientechnologies.orient.core.OConstants;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.peruncs.odb.api.ODBManagedConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static javax.resource.spi.ConnectionEvent.*;

class ODBManagedConnectionImpl implements ODBManagedConnection, Closeable {

    private static final Log log = LogFactory.getLog(ODBManagedConnectionImpl.class);

    private final ODatabaseSession session;
    private final ODBManagedConnectionFactoryImpl mcf;
    private PrintWriter logWriter = new PrintWriter(System.out);
    private final List<ConnectionEventListener> listeners = new ArrayList<>();
    private final ConnectionRequestInfo cri;
    private ODBConnectionImpl connection;

    public ODBManagedConnectionImpl(ODBManagedConnectionFactoryImpl mcf, ConnectionRequestInfo cri){
        this.mcf = mcf;
        this.cri = cri;
        session = mcf.newSession();
    }

    @Override
    public void destroy() {
        log.debug("destroy()");
        session.close();
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        log.debug("getConnection()");
        if(connection == null) {
            connection = new ODBConnectionImpl(this);
        }
        return connection;
    }

    ODatabaseSession getSession() {
        return session;
    }

    @Override
    public void cleanup() {
        log.debug("cleanup()");
        this.connection = null;
    }

    @Override
    public LocalTransaction getLocalTransaction() {
        log.debug("getLocalTransaction()");
        return new OrientLocalTransaction();
    }

    @Override
    public void associateConnection(Object connection)  {
        log.debug("associateConnection()");
        this.connection = (ODBConnectionImpl) connection;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        log.debug("addConnectionEventListener()");
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        log.debug("removeConnectionEventListener()");
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new ResourceException("OrientDB resource adapter does not support XA transactions");
    }

    class OrientLocalTransaction implements LocalTransaction {

        @Override
        public void begin() {
            log.debug("begin()");
            session.begin();
            fireConnectionEvent(LOCAL_TRANSACTION_STARTED);
        }

        @Override
        public void commit() {
            log.debug("commit()");
            session.commit();
            fireConnectionEvent(LOCAL_TRANSACTION_COMMITTED);
        }

        @Override
        public void rollback() {
            log.debug("rollback()");
            session.rollback();
            fireConnectionEvent(LOCAL_TRANSACTION_ROLLEDBACK);
        }
    }

    @Override
    public ManagedConnectionMetaData getMetaData() {

        return new ManagedConnectionMetaData(){
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
            public String getUserName()  {
                return "admin";
            }

        };

    }

    /**
     * Do not close the underlying connection now, as it may be used in a container-managed
     * transaction. The connection will be closed in {@link #cleanup()}.
     */
    @Override
    public void close() {
        log.debug("close()");
        fireConnectionEvent(CONNECTION_CLOSED);
    }

    private void fireConnectionEvent(int event) {
        log.debug("fireConnectionEvent()");
        ConnectionEvent connectionEvent = new ConnectionEvent(this, event);
        connectionEvent.setConnectionHandle(connection);
        synchronized (listeners) {
            for (ConnectionEventListener listener : this.listeners) {
                switch (event) {
                    case LOCAL_TRANSACTION_STARTED:
                        listener.localTransactionStarted(connectionEvent);
                        break;
                    case LOCAL_TRANSACTION_COMMITTED:
                        listener.localTransactionCommitted(connectionEvent);
                        break;
                    case LOCAL_TRANSACTION_ROLLEDBACK:
                        listener.localTransactionRolledback(connectionEvent);
                        break;
                    case CONNECTION_CLOSED:
                        listener.connectionClosed(connectionEvent);
                        break;
                    case CONNECTION_ERROR_OCCURRED:
                        listener.connectionErrorOccurred(connectionEvent);
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

    @Override
    public void setLogWriter(PrintWriter out){
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter(){
        return logWriter;
    }

}
