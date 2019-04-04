package org.kuplkris.accounts.main;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import org.kuplkris.accounts.entity.Account;
import org.kuplkris.accounts.exception.ApplicationRunningException;
import org.kuplkris.accounts.exception.ConnectionAcquireTimeOutException;
import org.kuplkris.accounts.exception.DatabaseConnectionException;
import org.kuplkris.accounts.exception.LoadPropertyFileException;
import org.kuplkris.accounts.exception.NoAccountException;
import org.kuplkris.accounts.exception.NotEnoughMoneyException;
import org.kuplkris.accounts.exception.SendMoneyException;
import org.kuplkris.accounts.exception.SqlOperationException;
import org.kuplkris.accounts.request.SendMoneyRequest;
import org.kuplkris.accounts.service.AccountOperationsService;
import org.kuplkris.accounts.service.DatabaseCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.ValidationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;
import static org.kuplkris.accounts.main.PropertiesHandler.getApplicationPort;
import static org.kuplkris.accounts.main.PropertiesHandler.getRequestPostParameter;

public final class MainApp extends NanoHTTPD {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Gson GSON = new Gson();

    private final AccountOperationsService accountOperationsService = new AccountOperationsService();

    public MainApp() {
        super(getApplicationPort());

        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            LOGGER.info("Application is running!");
        } catch (IOException e) {
            LOGGER.error("Can not start application", e);
            throw new ApplicationRunningException(e);
        }
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(final String[] args) {
        loadPropertyFile(args);

        try {
            DatabaseCreationService.createAndFillDatabase();
        } catch (Exception e) {
            LOGGER.error("Can not create database!", e);
        }

        new MainApp();
    }

    @Override
    public Response serve(final IHTTPSession session) {
        final Method method = session.getMethod();

        if (Method.PUT == method || Method.POST == method) {
            return processPostRequest(session);
        }
        if (Method.GET == method) {
            return processGetRequest(session);
        }

        return NanoHTTPD.newFixedLengthResponse("Unexpected request");
    }

    private static void loadPropertyFile(final String[] args) {
        if (args.length == 0) {
            LOGGER.info("Using default property file");

            final ClassLoader classLoader = MainApp.class.getClassLoader();
            try (final InputStream inputStream = classLoader.getResourceAsStream("application.properties")) {
                PropertiesHandler.loadProperties(inputStream);
            } catch (IOException e) {
                throw new LoadPropertyFileException("Can not find PROPERTIES file!", e);
            }
        } else {
            try (final InputStream inputStream = new FileInputStream(Paths.get(args[0]).toFile())) {
                PropertiesHandler.loadProperties(inputStream);
            } catch (IOException e) {
                throw new LoadPropertyFileException("Can not find PROPERTIES file!", e);
            }
        }
    }

    private static SendMoneyRequest getAndValidateRequest(final IHTTPSession session) throws ValidationException, IOException, ResponseException {
        final Map<String, String> parameters = new HashMap<>();

        session.parseBody(parameters);

        final SendMoneyRequest request;
        try {
            request = GSON.fromJson(parameters.get(getRequestPostParameter()), SendMoneyRequest.class);
            LOGGER.debug("Request: {}", request);
        } catch (Exception e) {
            LOGGER.error("Can not parse request", e);
            throw new ValidationException("Can not parse request", e);
        }

        if (request.getSum() <= 0) {
            throw new ValidationException("Incorrect sum");
        }

        return request;
    }

    private Response processGetRequest(final IHTTPSession session) {
        final Map<String, String> params = session.getParms();
        final String accountParam = params.get("accountId");
        if (accountParam == null) {
            return NanoHTTPD.newFixedLengthResponse("Incorrect request");
        } else {
            try {
                final Long accountId = Long.parseLong(accountParam);
                final Account account = accountOperationsService.getAccountById(accountId);
                LOGGER.info("Account {}", account);

                return NanoHTTPD.newFixedLengthResponse(account.toString());
            } catch (ConnectionAcquireTimeOutException | DatabaseConnectionException | SqlOperationException | NoAccountException e) {
                LOGGER.error("Exception {}", e.getMessage(), e);
                return NanoHTTPD.newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, e.getMessage());
            }
        }
    }

    private Response processPostRequest(final IHTTPSession session) {
        final SendMoneyRequest request;
        try {
            request = getAndValidateRequest(session);
        } catch (ValidationException e) {
            return NanoHTTPD.newFixedLengthResponse(BAD_REQUEST, MIME_PLAINTEXT, e.getMessage());
        } catch (IOException | ResponseException e) {
            return NanoHTTPD.newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, e.getMessage());
        }

        return processRequest(request);
    }

    private Response processRequest(final SendMoneyRequest request) {
        try {
            LOGGER.info("Send money from account {} to account {} sum {}", request.getFromAccountId(), request.getToAccountId(), request.getSum());
            accountOperationsService.sendMoney(request.getFromAccountId(), request.getToAccountId(), request.getSum());

            return NanoHTTPD.newFixedLengthResponse("Operation was finished successfully!");
        } catch (NotEnoughMoneyException | SqlOperationException | ConnectionAcquireTimeOutException
                | DatabaseConnectionException | NoAccountException | SendMoneyException e) {
            LOGGER.error("Exception {}", e.getMessage(), e);
            return NanoHTTPD.newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT, e.getMessage());
        }
    }

}
