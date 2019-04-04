package org.kuplkris.accounts.dao;

import org.kuplkris.accounts.exception.NoJdbcDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.kuplkris.accounts.main.PropertiesHandler.getDatabasePassword;
import static org.kuplkris.accounts.main.PropertiesHandler.getDatabaseUrl;
import static org.kuplkris.accounts.main.PropertiesHandler.getDatabaseUser;
import static org.kuplkris.accounts.main.PropertiesHandler.getJdbcDriver;
import static org.kuplkris.accounts.main.PropertiesHandler.getMaxPoolResources;

public final class DBConnectionsPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static volatile DBConnectionsPool instance;
    private final Semaphore sem = new Semaphore(getMaxPoolResources(), true);
    private final Queue<Connection> resources = new ConcurrentLinkedQueue<>();

    private DBConnectionsPool() {
        try {
            Class.forName(getJdbcDriver());
        } catch (ClassNotFoundException e) {
            throw new NoJdbcDriverException("Error while getting jdbc driver!", e);
        }
    }

    public Connection getResource(final long maxWaitMillis) throws InterruptedException, SQLException {
        sem.tryAcquire(maxWaitMillis, TimeUnit.MILLISECONDS);

        final Connection res = resources.poll();
        if (res != null) {
            return res;
        }

        try {
            return createConnection();
        } catch (SQLException e) {
            sem.release();
            throw e;
        }
    }

    public void returnResource(final Connection res) {
        resources.add(res);
        sem.release();
    }

    public void closeAllConnections() {
        for (final Connection resource : resources) {
            try {
                resource.close();
            } catch (SQLException e) {
                LOGGER.error("Can not close connection!", e);
            }
        }
    }

    public static DBConnectionsPool getInstance() {
        //noinspection StaticVariableUsedBeforeInitialization
        DBConnectionsPool localInstance = instance;
        if (localInstance == null) {
            synchronized (DBConnectionsPool.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBConnectionsPool();
                }
            }
        }
        return localInstance;
    }

    private static Connection createConnection() throws SQLException {
        //noinspection CallToDriverManagerGetConnection
        final Connection connection = DriverManager.getConnection(
                getDatabaseUrl(), getDatabaseUser(), getDatabasePassword());
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        return connection;
    }
}