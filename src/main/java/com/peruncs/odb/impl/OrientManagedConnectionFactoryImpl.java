package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.peruncs.odb.api.OrientDatabaseConnection;
import com.peruncs.odb.api.OrientDatabaseConnectionFactory;
import com.peruncs.odb.api.OrientManagedConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;


@ConnectionDefinition(
    connectionFactory = OrientDatabaseConnectionFactory.class,
    connectionFactoryImpl = OrientDatabaseConnectionFactoryImpl.class, 
    connection = OrientDatabaseConnection.class,
    connectionImpl = OrientDatabaseConnectionImpl.class)
public class OrientManagedConnectionFactoryImpl implements OrientManagedConnectionFactory {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(OrientManagedConnectionFactoryImpl.class);
    
    private PrintWriter logWriter = new PrintWriter(System.out);

    private OrientResourceAdapter ra;

    @ConfigProperty
    private String connectionUrl;
    
    @ConfigProperty
    private String serverUserName;

    @ConfigProperty
    private String serverPassword;

    @ConfigProperty
    private String dbName;

    @ConfigProperty
    private String dbType ;

    @ConfigProperty
    private String dbUsername;
    
    @ConfigProperty
    private String dbPassword;

    @ConfigProperty
    private int maxPoolSize =0 ;

    @ConfigProperty
    private boolean createDbIfMissing = true;

    private final int hash;

    public OrientManagedConnectionFactoryImpl(){
        hash = Objects.hash(connectionUrl,serverUserName,serverPassword, dbName, dbUsername, dbPassword);
    }
    
    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.debug("creating managed connection factory, url: "+connectionUrl+",  user: "+ dbUsername);
        validate();
        return new OrientDatabaseConnectionFactoryImpl(this, cxManager);
    }
    
    private void validate() throws ResourceException {
        if (connectionUrl == null || connectionUrl.trim().isEmpty()) {
            throw new ResourceException("configuration property [connectionUrl] must not be empty");
        }
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        throw new ResourceException("unmanaged environments are not supported");
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        log.debug("creating managed connection, url: "+connectionUrl+",  user: "+ dbUsername);
        return new OrientManagedConnectionImpl(this, cxRequestInfo);
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "resource" })
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo){

        for (ManagedConnection connection : (Set<ManagedConnection>) connectionSet) {
            if (connection instanceof OrientManagedConnectionImpl) {
                OrientManagedConnectionImpl orientConnection = (OrientManagedConnectionImpl) connection;
                ConnectionRequestInfo cri = orientConnection.getConnectionRequestInfo();
                if (cri == null || cri.equals(cxRequestInfo)) {
                    return connection;
                }
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out)  {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra)  {
        this.ra = (OrientResourceAdapter) ra;
    }

    @Override
    public TransactionSupport.TransactionSupportLevel getTransactionSupport() {
        return TransactionSupport.TransactionSupportLevel.LocalTransaction;
    }

    @Override
    public int hashCode() {
        return hash;
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

        OrientManagedConnectionFactoryImpl other = (OrientManagedConnectionFactoryImpl) obj;

        return     Objects.equals(connectionUrl,other.connectionUrl)
                && Objects.equals(serverUserName,other.serverUserName)
                && Objects.equals(serverPassword,other.serverPassword)
                && Objects.equals(dbName,other.dbName)
                && Objects.equals(dbUsername,other.dbUsername)
                && Objects.equals(dbPassword,other.dbPassword)
                ;
    }

    int getMaxPoolSize() {
        return maxPoolSize;
    }

    OrientDB newOrientDB(){
        OrientDB orientDb;

        if(serverUserName!=null && serverPassword!=null) {
            orientDb = new OrientDB(connectionUrl, serverUserName, serverPassword, OrientDBConfig.defaultConfig());
        }else {
            orientDb = new OrientDB(connectionUrl, OrientDBConfig.defaultConfig());
        }

        if(createDbIfMissing){
            orientDb.createIfNotExists(dbName,getDbType()) ;
        }

        return orientDb;
    }

    ODatabasePool newOrientDBPool(OrientDB orientDB){
        return new ODatabasePool(orientDB,dbName,dbUsername,dbPassword);
    }

    private ODatabaseType getDbType(){
        return dbType == null? ODatabaseType.MEMORY: ODatabaseType.valueOf(dbType);
    }

}