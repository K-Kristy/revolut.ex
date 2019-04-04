package org.kuplkris.accounts.dao;

import org.kuplkris.accounts.entity.Account;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SendMoneyException;

import java.sql.Connection;
import java.sql.SQLException;


@SuppressWarnings("JavaDoc")
public interface AccountDao {

    /**
     * @param accountId
     * @param connection
     * @return Account's information
     * @throws SQLException       if some SQL exceptions was thrown during sql query execution
     * @throws NoAccountException if there are any account with accountId in the database
     */
    Account getAccountById(final long accountId, final Connection connection) throws SQLException, NoAccountException;

    /**
     * @return New database connection
     * @throws InterruptedException if all attempts to get connection from the connection's pool were failed
     * @throws SQLException         if some SQL exceptions was thrown during connection's creation
     */
    Connection getConnection() throws InterruptedException, SQLException;

    /**
     * Returns connection to the connection's pool
     *
     * @param connection
     */
    void returnConnection(final Connection connection);

    /**
     * @param accountId
     * @param sum
     * @param connection
     * @throws SQLException       if some SQL exceptions was thrown during updating account's rows
     * @throws NoAccountException if there are any account with accountId in the database
     */
    void putMoney(final long accountId, final double sum, final Connection connection) throws SQLException, NoAccountException, SendMoneyException;

    /**
     * @param accountId
     * @param sum
     * @param connection
     * @throws NotEnoughMoneyException if account doesn't have less money then sum to send
     * @throws SQLException            if some SQL exceptions was thrown during updating account's rows
     * @throws NoAccountException      if there are any account with accountId in the database
     */
    void withdrawMoney(final long accountId, final double sum, final Connection connection)
            throws NotEnoughMoneyException, SQLException, NoAccountException;

}
