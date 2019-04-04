package org.kuplkris.accounts.service;

import org.kuplkris.accounts.dao.AccountDao;
import org.kuplkris.accounts.dao.AccountDaoImpl;
import org.kuplkris.accounts.entity.Account;
import org.kuplkris.accounts.exception.ConnectionAcquireTimeOutException;
import org.kuplkris.accounts.exception.DatabaseConnectionException;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SendMoneyException;
import org.kuplkris.accounts.exception.SqlOperationException;

import java.sql.Connection;
import java.sql.SQLException;

public class AccountOperationsService {

    private final AccountDao accountDao = new AccountDaoImpl();

    public void sendMoney(final long fromAccountId, final long toAccountId, final double sum)
            throws ConnectionAcquireTimeOutException, DatabaseConnectionException,
            SqlOperationException, NotEnoughMoneyException, NoAccountException, SendMoneyException {
        final Connection connection = getConnection();

        try {
            if (fromAccountId > toAccountId) {
                accountDao.withdrawMoney(fromAccountId, sum, connection);
                accountDao.putMoney(toAccountId, sum, connection);
            } else {
                accountDao.putMoney(toAccountId, sum, connection);
                accountDao.withdrawMoney(fromAccountId, sum, connection);
            }

            connection.commit();
        } catch (SQLException e) {
            final SqlOperationException sqlException = new SqlOperationException("Can not send money!", e);
            doRollback(connection, sqlException);

            throw sqlException;
        } catch (NotEnoughMoneyException | NoAccountException | SendMoneyException e) {
            doRollback(connection, e);
            throw e;
        } finally {
            accountDao.returnConnection(connection);
        }
    }


    public Account getAccountById(final long accountId) throws ConnectionAcquireTimeOutException,
            DatabaseConnectionException, SqlOperationException, NoAccountException {
        final Connection connection = getConnection();
        try {
            return accountDao.getAccountById(accountId, connection);
        } catch (SQLException e) {
            throw new SqlOperationException("Can not get balance. Please try later!", e);
        } finally {
            accountDao.returnConnection(connection);
        }
    }

    private Connection getConnection() throws ConnectionAcquireTimeOutException, DatabaseConnectionException {
        final Connection connection;
        try {
            connection = accountDao.getConnection();
        } catch (InterruptedException e) {
            throw new ConnectionAcquireTimeOutException("No free database connections", e);
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Can not create database connection!", e);
        }
        return connection;
    }

    private static void doRollback(final Connection connection, final Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            exception.addSuppressed(ex);
        }
    }

}
