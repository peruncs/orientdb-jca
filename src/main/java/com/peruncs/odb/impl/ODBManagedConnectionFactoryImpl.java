package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.peruncs.odb.api.ODBConnection;
import com.peruncs.odb.api.ODBConnectionFactory;
import com.peruncs.odb.api.ODBManagedConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;


@ConnectionDefinition(
        connectionFactory = ODBConnectionFactory.class,
        connectionFactoryImpl = ODBConnectionFactoryImpl.class,
        connection = ODBConnection.class,
        connectionImpl = ODBConnectionImpl.class)
public class ODBManagedConnectionFactoryImpl implements ODBManagedConnectionFactory {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ODBManagedConnectionFactoryImpl.class);

    private PrintWriter logWriter = new PrintWriter(System.out);

    private ODBResourceAdapter ra;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @ConfigProperty
    private String connectionUrl;

    @ConfigProperty
    private String serverUserName;

    @ConfigProperty
    private String serverPassword;

    @ConfigProperty
    private String dbName;

    @ConfigProperty
    private String dbType;

    @ConfigProperty
    private String dbUsername;

    @ConfigProperty
    private String dbPassword;

    @ConfigProperty(type = Integer.class)
    private Integer maxPoolSize = 0;

    @ConfigProperty(type = Boolean.class)
    private Boolean createDbIfMissing = true;

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getServerUserName() {
        return serverUserName;
    }

    public void setServerUserName(String serverUserName) {
        this.serverUserName = serverUserName;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbType() {
        return dbType;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Boolean getCreateDbIfMissing() {
        return createDbIfMissing;
    }

    public void setCreateDbIfMissing(Boolean createDbIfMissing) {
        this.createDbIfMissing = createDbIfMissing;
    }

    public ODBManagedConnectionFactoryImpl() {
        Objects.hash(connectionUrl, serverUserName, serverPassword, dbName, dbUsername, dbPassword);
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.debug("creating managed connection factory, url: " + connectionUrl + ",  user: " + dbUsername);
        validate();
        return new ODBConnectionFactoryImpl(this, cxManager);
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
        log.debug("creating managed connection, url: " + connectionUrl + ",  user: " + dbUsername);
        return new ODBManagedConnectionImpl(this, cxRequestInfo);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "resource"})
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) {

        for (ManagedConnection connection : (Set<ManagedConnection>) connectionSet) {
            if (connection instanceof ODBManagedConnectionImpl) {
                ODBManagedConnectionImpl orientConnection = (ODBManagedConnectionImpl) connection;
                ConnectionRequestInfo cri = orientConnection.getConnectionRequestInfo();
                if (cri == null || cri.equals(cxRequestInfo)) {
                    return connection;
                }
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
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
    public void setResourceAdapter(ResourceAdapter ra) {
        this.ra = (ODBResourceAdapter) ra;
    }

    @Override
    public TransactionSupport.TransactionSupportLevel getTransactionSupport() {
        return TransactionSupport.TransactionSupportLevel.LocalTransaction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionUrl, serverUserName, serverPassword, dbName, dbUsername, dbPassword);
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

        ODBManagedConnectionFactoryImpl other = (ODBManagedConnectionFactoryImpl) obj;

        return Objects.equals(connectionUrl, other.connectionUrl)
                && Objects.equals(serverUserName, other.serverUserName)
                && Objects.equals(serverPassword, other.serverPassword)
                && Objects.equals(dbName, other.dbName)
                && Objects.equals(dbUsername, other.dbUsername)
                && Objects.equals(dbPassword, other.dbPassword)
                ;
    }


    OrientDB newOrientDB() {
        OrientDB orientDb;

        if (serverUserName != null && serverPassword != null) {
            orientDb = new OrientDB(connectionUrl, serverUserName, serverPassword, OrientDBConfig.defaultConfig());
        } else {
            orientDb = new OrientDB(connectionUrl, OrientDBConfig.defaultConfig());
        }

        if (Boolean.TRUE.equals(createDbIfMissing)) {
            orientDb.createIfNotExists(dbName, inferDbType());
        }

        return orientDb;
    }

    ODatabasePool newOrientDBPool(OrientDB orientDB) {
        return new ODatabasePool(orientDB, dbName, dbUsername, dbPassword);
    }

    public ODatabaseType inferDbType() {
        return dbType == null ? ODatabaseType.MEMORY : ODatabaseType.valueOf(dbType);
    }

}