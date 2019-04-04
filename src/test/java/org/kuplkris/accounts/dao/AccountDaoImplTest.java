package org.kuplkris.accounts.dao;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuplkris.accounts.entity.Account;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SendMoneyException;
import org.kuplkris.accounts.main.PropertiesHandler;
import org.kuplkris.accounts.service.DatabaseCreationService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.fail;
import static org.kuplkris.accounts.main.PropertiesHandler.getMaxWaitMillis;

public class AccountDaoImplTest {

    private static final long ACCOUNT_ID_1 = 1;
    private static final double AMOUNT_1 = 100;
    private static final long ACCOUNT_ID_2 = 2;
    private static final long AMOUNT_2 = 200;
    private static final long ACCOUNT_ID_3 = 3;
    private static final long AMOUNT_3 = 300;
    private static final long ACCOUNT_ID_4 = 4;
    private static final long AMOUNT_4 = 400;
    private static final long ACCOUNT_ID_5 = 5;
    private static final long SUM = 100;
    private static final long NONEXISTENT_ACCOUNT_ID = 11111;

    private final AccountDao accountDao = new AccountDaoImpl();
    private final DBConnectionsPool pool = DBConnectionsPool.getInstance();

    @BeforeClass
    public static void init() throws SQLException, InterruptedException, IOException {
        final ClassLoader classLoader = AccountDaoImplTest.class.getClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream("application-test.properties")) {
            PropertiesHandler.loadProperties(inputStream);
        }

        DatabaseCreationService.createAndFillTestDatabase();
    }

    @AfterClass
    public static void down() throws SQLException, InterruptedException {
        DatabaseCreationService.cleanTestDatabase();
    }

    @Test
    public void getAccountByIdTest() throws Exception {
        final Account expectedAccount = new Account(ACCOUNT_ID_1, AMOUNT_1);

        final Connection con = pool.getResource(getMaxWaitMillis());
        final Account actualAccount = accountDao.getAccountById(ACCOUNT_ID_1, con);

        Assert.assertEquals(expectedAccount, actualAccount);
        pool.returnResource(con);
    }

    @Test(expected = NoAccountException.class)
    public void getAccountByIdAccountNotFoundTest() throws Exception {
        final Connection con = pool.getResource(getMaxWaitMillis());

        accountDao.getAccountById(NONEXISTENT_ACCOUNT_ID, con);
        pool.returnResource(con);
    }

    @Test
    public void putMoneyTest() throws Exception {
        final Account expectedAccount = new Account(ACCOUNT_ID_2, AMOUNT_2 + SUM);

        final Connection con = pool.getResource(getMaxWaitMillis());
        accountDao.putMoney(ACCOUNT_ID_2, SUM, con);
        con.commit();
        final Account actualAccount = accountDao.getAccountById(ACCOUNT_ID_2, con);

        Assert.assertEquals(expectedAccount, actualAccount);
        pool.returnResource(con);
    }

    @Test(expected = NoAccountException.class)
    public void putMoneyAccountNotFoundTest() throws Exception {
        final Connection con = pool.getResource(getMaxWaitMillis());
        accountDao.putMoney(NONEXISTENT_ACCOUNT_ID, SUM, con);
    }

    @Test
    public void putMoneyConnectionIssueTest() throws Exception {
        final Connection resource = pool.getResource(getMaxWaitMillis());
        final Account expected = accountDao.getAccountById(ACCOUNT_ID_3, resource);
        final Connection con = pool.getResource(getMaxWaitMillis());
        con.close();

        try {
            accountDao.putMoney(ACCOUNT_ID_3, AMOUNT_3, con);
        } catch (SQLException e) {
            final Account actual = accountDao.getAccountById(ACCOUNT_ID_3, resource);
            Assert.assertEquals(expected, actual);
            return;
        }

        fail();

    }

    @Test
    public void withdrawMoneyTest() throws Exception {
        final Account expectedAccount = new Account(ACCOUNT_ID_4, AMOUNT_4 - SUM);

        final Connection con = pool.getResource(getMaxWaitMillis());
        accountDao.withdrawMoney(ACCOUNT_ID_4, SUM, con);
        con.commit();

        final Account actualAccount = accountDao.getAccountById(ACCOUNT_ID_4, con);
        Assert.assertEquals(expectedAccount, actualAccount);
        pool.returnResource(con);
    }

    @SuppressWarnings("NumericOverflow")
    @Test
    public void putMoneyTooMuchMoneyTest() throws Exception {
        final Connection con = pool.getResource(getMaxWaitMillis());
        final Account expected = accountDao.getAccountById(ACCOUNT_ID_5, con);

        try {
            accountDao.putMoney(ACCOUNT_ID_5, Double.MAX_VALUE * 2, con);
        } catch (SendMoneyException e) {
            final Account actual = accountDao.getAccountById(ACCOUNT_ID_5, con);
            Assert.assertEquals(expected, actual);
            return;
        }

        fail();
    }

    @Test(expected = NoAccountException.class)
    public void withdrawMoneyAccountNotFoundTest() throws Exception {
        final Connection con = pool.getResource(getMaxWaitMillis());
        accountDao.withdrawMoney(NONEXISTENT_ACCOUNT_ID, SUM, con);
    }

    @Test
    public void withdrawMoneyNotEnoughMoneyTest() throws Exception {
        final Connection con = pool.getResource(getMaxWaitMillis());
        final Account expected = accountDao.getAccountById(ACCOUNT_ID_4, con);

        try {
            accountDao.withdrawMoney(ACCOUNT_ID_4, expected.getAmount() + SUM, con);
        } catch (NotEnoughMoneyException e) {
            final Account actual = accountDao.getAccountById(ACCOUNT_ID_4, con);
            Assert.assertEquals(expected, actual);
            return;
        }

        fail();
    }

    @Test
    public void withdrawMoneyConnectionIssueTest() throws Exception {
        final Connection resource = pool.getResource(getMaxWaitMillis());
        final Account expected = accountDao.getAccountById(ACCOUNT_ID_4, resource);
        final Connection con = pool.getResource(getMaxWaitMillis());
        con.close();

        try {
            accountDao.withdrawMoney(ACCOUNT_ID_4, SUM, con);
        } catch (SQLException e) {
            final Account actual = accountDao.getAccountById(ACCOUNT_ID_4, resource);
            Assert.assertEquals(expected, actual);
            return;
        }

        fail();
    }
}

