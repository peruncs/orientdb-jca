package com.peruncs.odb.impl;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.peruncs.odb.api.ODBConnectionFactory;
import com.peruncs.odb.api.ODBManagedConnectionFactory;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;


@ConnectionDefinition(
        connectionFactory = ODBConnectionFactory.class,
        connectionFactoryImpl = ODBConnectionFactoryImpl.class,
        connection = ODatabaseSession.class,
        connectionImpl = ODatabaseSession.class)
public class ODBManagedConnectionFactoryImpl implements ODBManagedConnectionFactory {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(ODBManagedConnectionFactoryImpl.class.getSimpleName());

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

    @ConfigProperty(type = Integer.class)
    private Integer maxRetry = 3;

    public ODBManagedConnectionFactoryImpl() {
    }

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

    public Integer getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(Integer maxRetry) {
        this.maxRetry = maxRetry;
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

    @Override
    public ODBConnectionFactory createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.log(Level.FINER, () -> "creating managed connection factory with connection manager, url: " + url + ",  user: " + username);
        validate();
        return new ODBConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public ODBConnectionFactory createConnectionFactory() throws ResourceException {
        log.log(Level.FINER, () -> "creating managed connection factory, url: " + url + ",  user: " + username);
        validate();
        return new ODBConnectionFactoryImpl(this, null);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        log.log(Level.FINER, () -> "Creating managed connection, url: " + url + ",  user: " + username);
        return new ODBManagedConnectionImpl(this, cxRequestInfo);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) {
        for (ManagedConnection connection : (Set<ManagedConnection>) connectionSet) {
            if (connection instanceof ODBManagedConnectionImpl) {
                ODBManagedConnectionImpl orientConnection = (ODBManagedConnectionImpl) connection;
                ConnectionRequestInfo cri = orientConnection.getConnectionRequestInfo();
                if (Objects.equals(cri, cxRequestInfo)) {
                    return connection;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to create an ODB session from a session pool. If the pool is bad, it is replaced and re-tried.
     *
     * @return ODB connection
     * @throws ResourceException after max allowed attempts to create a session have been exhausted.
     */
    @Override
    public ODatabaseSession newSession() throws ResourceException {

        Map<String, ODatabasePool> pools = ra.getPool();

        synchronized (pools) {
            for (int i = 0; i < maxRetry; i++) {
                final int currentAttempt = i;
                ODatabasePool currentPool = null;
                try {
                    currentPool = pools.get(url);
                    if (currentPool == null) {
                        pools.put(url, currentPool = new ODatabasePool(url, username, password, OrientDBConfig.defaultConfig()));
                        log.info(() -> "ODB-JCA created database pool: " + url + ", on attempt: " + currentAttempt);
                    }
                    return currentPool.acquire();
                } catch (Exception e) {
                    log.log(Level.INFO, "ODB-JCA (discarded) failed database pool: " + url, e);
                    pools.remove(url);
                    if (currentPool != null) {
                        // Close the  failed pool async, and get out of the lock asap.
                        final ODatabasePool failedPool = currentPool;
                        CompletableFuture.runAsync(() -> {
                            try {
                                failedPool.close();
                            } catch (Exception ex) {
                            }
                        });
                    }
                }
            }
        }

        throw new ResourceException("Unable to obtain session from: " + url + ", after: " + maxRetry + " attempts");

    }

//    @Override
//    public ODatabaseSession newSession() throws ResourceException {
//        Map<String, ODatabasePool> pools = ra.getPool();
//        synchronized (pools) {
//            try {
//                return pools.computeIfAbsent(url, k -> {
//                    log.info("ODB-JCA created database pool: " + k);
//                    return new ODatabasePool(url, username, password, OrientDBConfig.defaultConfig());
//                }).acquire();
//            } catch (Exception e) {
//                ODatabasePool newPool = new ODatabasePool(url, username, password, OrientDBConfig.defaultConfig());
//                ODatabasePool failedPool = pools.replace(url, newPool);
//                if (failedPool != null)
//                    CompletableFuture.runAsync(() -> {
//                        try {
//                            failedPool.close();
//                        } catch (Exception ex) {
//                        }
//                    });
//                try {
//                    return newPool.acquire();
//                }catch(Exception ex){
//                    throw new ResourceException("Unable to obtain session from: " + url);
//                }
//            }
//        }
//    }

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
}
