package com.peruncs.odb.impl;


import com.orientechnologies.orient.core.OConstants;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.OrientDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static javax.resource.spi.ConnectionEvent.*;


class OrientManagedConnectionImpl implements ManagedConnection, Closeable {

    private static Logger log = LoggerFactory.getLogger(OrientManagedConnectionImpl.class);

    private OrientManagedConnectionFactoryImpl mcf;
    private OrientDB orientDB;
    private ODatabasePool orientDBPool;
    private PrintWriter logWriter = new PrintWriter(System.out);
    private List<ConnectionEventListener> listeners = new ArrayList<ConnectionEventListener>();
    private ConnectionRequestInfo cri;
    private OrientDatabaseConnectionImpl connection;


    class OrientLocalTransaction implements LocalTransaction {

        @Override
        public void begin() throws ResourceException {
            log.debug("begin()");
            //db.begin();
            fireConnectionEvent(LOCAL_TRANSACTION_STARTED);
        }

        @Override
        public void commit() throws ResourceException {
            log.debug("commit()");
            //db.commit();
            fireConnectionEvent(LOCAL_TRANSACTION_COMMITTED);
        }

        @Override
        public void rollback() throws ResourceException {
            log.debug("rollback()");
            //db.rollback();
            fireConnectionEvent(LOCAL_TRANSACTION_ROLLEDBACK);
        }
    }

    public OrientManagedConnectionImpl(OrientManagedConnectionFactoryImpl mcf, ConnectionRequestInfo cri){
        this.mcf = mcf;
        this.cri = cri;
        orientDB = mcf.newOrientDB();
        orientDBPool = mcf.newOrientDBPool(orientDB);

    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.debug("getConnection()");
        if(connection == null) {
            connection = new OrientDatabaseConnectionImpl(this);
        }
        return connection;
    }

    @Override
    public void destroy()  {
        log.debug("destroy()");
        orientDBPool.close();
        orientDB.close();
    }

    @Override
    public void cleanup() throws ResourceException {
        log.debug("cleanup()");
        this.connection = null;
    }


    OrientDB getOrientDB() {
        return orientDB;
    }

    ODatabasePool getOrientDBPool() {
        return orientDBPool;
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        log.debug("associateConnection()");
        this.connection = (OrientDatabaseConnectionImpl) connection;
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

    @Override
    public LocalTransaction getLocalTransaction(){
        log.debug("getLocalTransaction()");
        return new OrientLocalTransaction();
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
