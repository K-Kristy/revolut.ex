package org.kuplkris.accounts.dao;

import org.kuplkris.accounts.dao.DatabaseConstants.AccountColumns;
import org.kuplkris.accounts.dao.DatabaseConstants.AccountQuery;
import org.kuplkris.accounts.entity.Account;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SendMoneyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.kuplkris.accounts.main.PropertiesHandler.getMaxAttempts;
import static org.kuplkris.accounts.main.PropertiesHandler.getMaxWaitMillis;

public class AccountDaoImpl implements AccountDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final DBConnectionsPool CONNECTIONS_POOL = DBConnectionsPool.getInstance();

    @Override
    public Account getAccountById(final long accountId, final Connection connection) throws SQLException, NoAccountException {
        try (final PreparedStatement ps = createPreparedStatement(connection, AccountQuery.SELECT_QUERY, accountId);
             final ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                final int id = rs.getInt(AccountColumns.ID);
                final double amount = rs.getDouble(AccountColumns.AMOUNT);
                return new Account(id, amount);
            } else {
                throw new NoAccountException("Account " + accountId + " does not exist");
            }
        }
    }

    @Override
    public Connection getConnection() throws InterruptedException, SQLException {
        Connection connection = null;
        int getResourceCounter = 1;
        int connectDatabaseCounter = 1;
        while (getResourceCounter <= getMaxAttempts() || connectDatabaseCounter <= getMaxAttempts()) {
            try {
                LOGGER.debug("Try to get connection. Existing resource attempt: " +
                                "{} from {}; new resource attempt: {} from {}",
                        getResourceCounter, getMaxAttempts(), connectDatabaseCounter, getMaxAttempts());
                connection = CONNECTIONS_POOL.getResource(getMaxWaitMillis());
                break;
            } catch (InterruptedException e) {
                LOGGER.error("Can not get resource", e);
                if (getResourceCounter == getMaxAttempts()) {
                    throw e;
                }

                getResourceCounter++;
            } catch (SQLException e) {
                LOGGER.error("Can not create resource", e);
                if (connectDatabaseCounter == getMaxAttempts()) {
                    throw e;
                }

                connectDatabaseCounter++;
            }
        }
        return connection;
    }

    @Override
    public void returnConnection(final Connection connection) {
        CONNECTIONS_POOL.returnResource(connection);
    }

    @Override
    public void putMoney(final long accountId, final double sum, final Connection connection) throws SQLException, NoAccountException, SendMoneyException {
        try (final PreparedStatement ps = createPreparedStatement(connection, AccountQuery.SELECT_FOR_UPDATE_QUERY, accountId);
             final ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                final double amount = rs.getDouble(AccountColumns.AMOUNT);
                checkIfTooMuchMoney(sum, amount);
                rs.updateDouble(AccountColumns.AMOUNT, amount + sum);
                rs.updateRow();
            } else {
                throw new NoAccountException("Account " + accountId + " does not exist");
            }
        }
    }

    @Override
    public void withdrawMoney(final long accountId, final double sum, final Connection connection)
            throws NotEnoughMoneyException, SQLException, NoAccountException {
        try (final PreparedStatement ps = createPreparedStatement(connection, AccountQuery.SELECT_FOR_UPDATE_QUERY, accountId);
             final ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                final double amount = rs.getDouble(AccountColumns.AMOUNT);
                if (amount < sum) {
                    throw new NotEnoughMoneyException("Not enough money on account " + accountId);
                }

                rs.updateDouble(AccountColumns.AMOUNT, amount - sum);
                rs.updateRow();
            } else {
                throw new NoAccountException("Account " + accountId + " does not exist");
            }
        }
    }

    private static PreparedStatement createPreparedStatement(final Connection con, final String query, final long accountId) throws SQLException {
        final PreparedStatement ps = con.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ps.setLong(1, accountId);
        return ps;
    }

    private static void checkIfTooMuchMoney(final double sum, final double amount) throws SendMoneyException {
        if (Double.isInfinite(amount + sum)) {
            throw new SendMoneyException("Too much money");
        }
    }
}
