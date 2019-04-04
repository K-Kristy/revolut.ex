package org.kuplkris.accounts.main;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuplkris.accounts.dao.AccountDao;
import org.kuplkris.accounts.dao.AccountDaoImpl;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SendMoneyException;
import org.kuplkris.accounts.service.AccountOperationServiceTest;
import org.kuplkris.accounts.service.AccountOperationsService;
import org.kuplkris.accounts.service.DatabaseCreationService;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class MainAppIntegrationTest {

    private static final long ACCOUNT_ID_1 = 1L;
    private static final long ACCOUNT_ID_2 = 2L;
    private static final long ACCOUNT_ID_3 = 3L;
    private static final long ACCOUNT_ID_4 = 4L;
    private static final long ACCOUNT_ID_5 = 5L;
    private static final int CODE_OK = 200;
    private static MainApp server;

    @BeforeClass
    public static void init() throws IOException, SQLException, InterruptedException {
        final ClassLoader classLoader = AccountOperationServiceTest.class.getClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream("application-test.properties")) {
            PropertiesHandler.loadProperties(inputStream);
        }

        DatabaseCreationService.createAndFillTestDatabase();
        server = new MainApp();
    }

    @SuppressWarnings({"CallToPrintStackTrace", "StaticVariableUsedBeforeInitialization"})
    @AfterClass
    public static void down() {
        if (server != null && server.isAlive()) {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void sendMoneyNoDeadLockOnCyclicTransferTest() throws Exception {
        final AccountDao accountDao = new AccountDaoImpl();
        final AccountDao accountDaoMock = getMockAccountDao(accountDao);

        addDelayForTransferMethods(accountDao, accountDaoMock, ACCOUNT_ID_2);

        final HttpResponse response1 = sendTransferMoneyRequest(ACCOUNT_ID_1, ACCOUNT_ID_2, 100);
        final HttpResponse response2 = sendTransferMoneyRequest(ACCOUNT_ID_2, ACCOUNT_ID_1, 30);

        Assert.assertEquals(CODE_OK, response1.getStatusLine().getStatusCode());
        Assert.assertEquals(CODE_OK, response2.getStatusLine().getStatusCode());

        final HttpUriRequest request3 = new HttpGet("http://localhost:8083/?accountId=1");
        final HttpUriRequest request4 = new HttpGet("http://localhost:8083/?accountId=2");

        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpResponse response3 = httpClient.execute(request3);
        final HttpResponse response4 = httpClient.execute(request4);

        Assert.assertEquals(CODE_OK, response3.getStatusLine().getStatusCode());
        Assert.assertEquals(CODE_OK, response4.getStatusLine().getStatusCode());

        Assert.assertEquals("accountId = 1 amount = 30.0", inputStreamToString(response3.getEntity().getContent()));
        Assert.assertEquals("accountId = 2 amount = 270.0", inputStreamToString(response4.getEntity().getContent()));
    }


    @Test
    public void sendMoneyFromOneAccountTwiceTest() throws Exception {
        final AccountDao accountDao = new AccountDaoImpl();
        final AccountDao accountDaoMock = getMockAccountDao(accountDao);

        addDelayForTransferMethods(accountDao, accountDaoMock, ACCOUNT_ID_3);

        final HttpResponse response1 = sendTransferMoneyRequest(ACCOUNT_ID_3, ACCOUNT_ID_4, 100);
        final HttpResponse response2 = sendTransferMoneyRequest(ACCOUNT_ID_3, ACCOUNT_ID_5, 200);

        Assert.assertEquals(CODE_OK, response1.getStatusLine().getStatusCode());
        Assert.assertEquals(CODE_OK, response2.getStatusLine().getStatusCode());

        final HttpUriRequest request3 = new HttpGet("http://localhost:8083/?accountId=3");
        final HttpUriRequest request4 = new HttpGet("http://localhost:8083/?accountId=4");
        final HttpUriRequest request5 = new HttpGet("http://localhost:8083/?accountId=5");

        final HttpResponse response3 = HttpClientBuilder.create().build().execute(request3);
        final HttpResponse response4 = HttpClientBuilder.create().build().execute(request4);
        final HttpResponse response5 = HttpClientBuilder.create().build().execute(request5);

        Assert.assertEquals(CODE_OK, response3.getStatusLine().getStatusCode());
        Assert.assertEquals(CODE_OK, response4.getStatusLine().getStatusCode());
        Assert.assertEquals(CODE_OK, response5.getStatusLine().getStatusCode());

        Assert.assertEquals("accountId = 3 amount = 0.0", inputStreamToString(response3.getEntity().getContent()));
        Assert.assertEquals("accountId = 4 amount = 500.0", inputStreamToString(response4.getEntity().getContent()));
        Assert.assertEquals("accountId = 5 amount = 700.0", inputStreamToString(response5.getEntity().getContent()));
    }

    @SuppressWarnings("ReturnOfNull")
    private static void addDelayForTransferMethods(final AccountDao accountDao, final AccountDao accountDaoMock, final long accountId)
            throws SQLException, NoAccountException, SendMoneyException, NotEnoughMoneyException {
        doAnswer(invocationOnMock -> {
            final Object[] arguments = invocationOnMock.getArguments();
            Thread.sleep(5000);
            accountDao.putMoney((long) arguments[0], (double) arguments[1], (Connection) arguments[2]);
            return null;
        }).when(accountDaoMock)
                .putMoney(eq(accountId), anyDouble(), any(Connection.class));

        doAnswer(invocationOnMock -> {
            Object[] arguments = invocationOnMock.getArguments();
            Thread.sleep(5000);
            accountDao.withdrawMoney((long) arguments[0], (double) arguments[1], (Connection) arguments[2]);
            return null;
        }).when(accountDaoMock).withdrawMoney(eq(accountId), anyDouble(), any(Connection.class));
    }

    private static void setFinalField(final Object source, final Field field, final Object newValue) throws Exception {
        field.setAccessible(true);

        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(source, newValue);
    }

    private static String inputStreamToString(final InputStream stream) throws IOException {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private static HttpResponse sendTransferMoneyRequest(final long fromAccount, final long toAccount, final double sum) throws IOException {
        final String request = "{\n" +
                "\t\"fromAccountId\":" + fromAccount + ",\n" +
                "\t\"toAccountId\":" + toAccount + ",\n" +
                "\t\"sum\":" + sum + "\t\n" +
                "}";

        final HttpEntity entity = new StringEntity(request, ContentType.APPLICATION_JSON);

        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost postRequest = new HttpPost("http://localhost:8083/");
        postRequest.setEntity(entity);

        return httpClient.execute(postRequest);
    }

    @SuppressWarnings({"ReturnOfNull", "CastToConcreteClass"})
    private static AccountDao getMockAccountDao(final AccountDao accountDao) throws Exception {
        final AccountDao accountDaoMock = spy(accountDao);
        final Field daoField = AccountOperationsService.class.getDeclaredField("accountDao");
        final Field serviceField = MainApp.class.getDeclaredField("accountOperationsService");

        daoField.setAccessible(true);
        serviceField.setAccessible(true);

        final AccountOperationsService service = (AccountOperationsService) serviceField.get(server);
        setFinalField(service, daoField, accountDaoMock);

        return accountDaoMock;
    }

}
