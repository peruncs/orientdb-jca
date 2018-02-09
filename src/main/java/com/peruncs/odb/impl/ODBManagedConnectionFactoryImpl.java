package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDBConfig;
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
        connection = ODatabaseSession.class,
        connectionImpl = ODatabaseSession.class)
public class ODBManagedConnectionFactoryImpl implements ODBManagedConnectionFactory {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ODBManagedConnectionFactoryImpl.class);

    private PrintWriter logWriter = new PrintWriter(System.out);

    private ODBResourceAdapter ra;

    @ConfigProperty
    private String url;

    @ConfigProperty
    private String username;

    @ConfigProperty
    private String password;

    @ConfigProperty(type = Integer.class)
    private Integer maxPoolSize = 0;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public ODBManagedConnectionFactoryImpl() {
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.debug("creating managed connection factory, url: " + url + ",  user: " + username);
        validate();
        return new ODBConnectionFactoryImpl(this, cxManager);
    }

    private void validate() throws ResourceException {

        if (url == null || url.trim().isEmpty()) {
            throw new ResourceException("configuration property [url] must not be empty");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new ResourceException("configuration property [username] must not be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ResourceException("configuration property [password] must not be empty");
        }

    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        throw new ResourceException("unmanaged environments are not supported");
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        log.debug("creating managed connection, url: " + url + ",  user: " + username);
        return new ODBManagedConnectionImpl(this, cxRequestInfo);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "resource"})
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) {

        for (ManagedConnection connection : (Set<ManagedConnection>) connectionSet) {
            if (connection instanceof ODBManagedConnectionImpl) {
                ODBManagedConnectionImpl orientConnection = (ODBManagedConnectionImpl) connection;
                ConnectionRequestInfo cri =  orientConnection.getConnectionRequestInfo();
                if (Objects.equals(cri,cxRequestInfo)) {
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
    public int hashCode() {
        return Objects.hash(url, username, password);
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

        return Objects.equals(url, other.url)
                && Objects.equals(username, other.username)
                && Objects.equals(password, other.password)
                ;
    }

    ODatabaseSession newSession() {
        return ra.getPool()
                .computeIfAbsent(url, k -> {
                    log.info("ODB-JCA created database pool: " + k);
                    return new ODatabasePool(url, username, password, OrientDBConfig.defaultConfig());
                })
                .acquire();
    }

}