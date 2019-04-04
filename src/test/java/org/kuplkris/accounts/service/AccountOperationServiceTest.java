package org.kuplkris.accounts.service;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuplkris.accounts.dao.AccountDao;
import org.kuplkris.accounts.entity.Account;
import org.kuplkris.accounts.exception.ConnectionAcquireTimeOutException;
import org.kuplkris.accounts.exception.DatabaseConnectionException;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SqlOperationException;
import org.kuplkris.accounts.main.PropertiesHandler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountOperationServiceTest {

    private static final long ACCOUNT_ID_1 = 1;
    private static final long ACCOUNT_ID_2 = 2;
    private static final long ACCOUNT_ID_3 = 3;
    private static final long ACCOUNT_ID_4 = 4;

    private final AccountOperationsService accountOperationsService = new AccountOperationsService();

    @BeforeClass
    public static void init() throws IOException {
        final ClassLoader classLoader = AccountOperationServiceTest.class.getClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream("application-test.properties")) {
            PropertiesHandler.loadProperties(inputStream);
        }
    }

    @AfterClass
    public static void down() throws SQLException, InterruptedException {
        DatabaseCreationService.cleanTestDatabase();
    }

    @Test
    public void getAccountByIdTest() throws Exception {
        final Account expectedAccount = new Account(ACCOUNT_ID_1, 10);
        final AccountDao accountDao = getMockAccountDao(getSimpleMockConnection());

        when(accountDao.getAccountById(eq(ACCOUNT_ID_1), any())).thenReturn(expectedAccount);

        final Account actualAccount = accountOperationsService.getAccountById(ACCOUNT_ID_1);
        Assert.assertEquals(expectedAccount, actualAccount);
    }

    @Test(expected = NoAccountException.class)
    public void getAccountByIdAccountNotFoundTest() throws Exception {
        final AccountDao accountDao = getMockAccountDao(getSimpleMockConnection());

        when(accountDao.getAccountById(eq(ACCOUNT_ID_2), any())).thenThrow(new NoAccountException(""));
        accountOperationsService.getAccountById(ACCOUNT_ID_2);
    }

    @Test(expected = SqlOperationException.class)
    public void getAccountByIdSqlExceptionTest() throws Exception {
        final AccountDao accountDao = getMockAccountDao(getSimpleMockConnection());

        when(accountDao.getAccountById(eq(ACCOUNT_ID_3), any())).thenThrow(new SQLException(""));
        accountOperationsService.getAccountById(ACCOUNT_ID_3);
    }

    @Test(expected = DatabaseConnectionException.class)
    public void getAccountByIdDatabaseConnectionExceptionTest() throws Exception {
        final AccountDao accountDao = getMockAccountDao(getSimpleMockConnection());
        when(accountDao.getConnection()).thenThrow(new SQLException(""));

        accountOperationsService.getAccountById(ACCOUNT_ID_4);
    }

    @Test(expected = ConnectionAcquireTimeOutException.class)
    public void getAccountByIdConnectionAcquireTimeOutExceptionTest() throws Exception {
        final AccountDao accountDao = getMockAccountDao(getSimpleMockConnection());
        when(accountDao.getConnection()).thenThrow(new InterruptedException(""));

        accountOperationsService.getAccountById(ACCOUNT_ID_4);
    }

    @Test
    public void sendMoneyFromAccountNotFoundTest() throws Exception {
        final Connection connection = getMockConnection();
        final AccountDao accountDao = getMockAccountDao(connection);

        doThrow(new NoAccountException("")).when(accountDao).withdrawMoney(anyLong(), anyDouble(), any(Connection.class));
        try {
            accountOperationsService.sendMoney(ACCOUNT_ID_1, ACCOUNT_ID_2, 10);
        } catch (NoAccountException e) {
            verify(connection).rollback();
            return;
        }

        fail();
    }

    @Test
    public void sendMoneyCanNotWithdrawMoneyTest() throws Exception {
        final Connection connection = getMockConnection();
        final AccountDao accountDao = getMockAccountDao(connection);

        doThrow(new SQLException("")).when(accountDao).withdrawMoney(anyLong(), anyDouble(), any(Connection.class));
        try {
            accountOperationsService.sendMoney(ACCOUNT_ID_1, ACCOUNT_ID_2, 10);
        } catch (SqlOperationException e) {
            verify(connection).rollback();
            return;
        }

        fail();
    }

    @Test
    public void sendMoneyNotEnoughMoneyExceptionTest() throws Exception {
        final Connection connection = getMockConnection();
        final AccountDao accountDao = getMockAccountDao(connection);

        doThrow(new NotEnoughMoneyException("")).when(accountDao).withdrawMoney(anyLong(), anyDouble(), any(Connection.class));
        try {
            accountOperationsService.sendMoney(ACCOUNT_ID_1, ACCOUNT_ID_2, 10);
        } catch (NotEnoughMoneyException e) {
            verify(connection).rollback();
            return;
        }

        fail();
    }

    @Test
    public void sendMoneyToAccountNotFoundTest() throws Exception {
        final Connection connection = getMockConnection();
        final AccountDao accountDao = getMockAccountDao(connection);

        doThrow(new NoAccountException("")).when(accountDao).putMoney(anyLong(), anyDouble(), any(Connection.class));
        try {
            accountOperationsService.sendMoney(ACCOUNT_ID_1, ACCOUNT_ID_2, 10);
        } catch (NoAccountException e) {
            verify(connection).rollback();
            return;
        }

        fail();
    }

    @Test
    public void sendMoneyCanNotPutMoneyTest() throws Exception {
        final Connection connection = getMockConnection();
        final AccountDao accountDao = getMockAccountDao(connection);

        doThrow(new SQLException("")).when(accountDao).putMoney(anyLong(), anyDouble(), any(Connection.class));
        try {
            accountOperationsService.sendMoney(ACCOUNT_ID_1, ACCOUNT_ID_2, 10);
        } catch (SqlOperationException e) {
            verify(connection).rollback();
            return;
        }

        fail();
    }

    @Test
    public void sendMoneyTest() throws Exception {
        final Connection connection = getMockConnection();
        final AccountDao accountDao = getMockAccountDao(connection);

        doNothing().when(accountDao).putMoney(anyLong(), anyDouble(), any(Connection.class));
        doNothing().when(accountDao).withdrawMoney(anyLong(), anyDouble(), any(Connection.class));

        accountOperationsService.sendMoney(ACCOUNT_ID_1, ACCOUNT_ID_2, 10);

        verify(connection).commit();
    }


    private AccountDao getMockAccountDao(final Connection connection) throws Exception {
        final AccountDao accountDao = mock(AccountDao.class);

        doNothing().when(accountDao).returnConnection(any());
        when(accountDao.getConnection()).thenReturn(connection);

        final Field declaredField = AccountOperationsService.class.getDeclaredField("accountDao");
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        declaredField.set(accountOperationsService, accountDao);

        declaredField.setAccessible(accessible);

        return accountDao;
    }

    private static Connection getSimpleMockConnection() throws SQLException {
        final Connection connection = mock(Connection.class);
        doNothing().when(connection).rollback();
        return connection;
    }


    private static Connection getMockConnection() throws SQLException {
        final Connection connection = getSimpleMockConnection();

        final PreparedStatement ps = mock(PreparedStatement.class);

        when(connection.prepareStatement(any(String.class), any(Integer.class), any(Integer.class))).thenReturn(ps);

        final ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true);
        when(rs.getDouble(any())).thenReturn(100.2);

        return connection;
    }
}
