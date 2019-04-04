package org.kuplkris.accounts.service;

import org.kuplkris.accounts.dao.DBConnectionsPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("UtilityClassCanBeEnum")
public final class DatabaseCreationService {

    private static final DBConnectionsPool POOL = DBConnectionsPool.getInstance();

    private DatabaseCreationService() {
    }

    public static void createAndFillDatabase() throws SQLException, InterruptedException {
        createAccountTable();

        final Connection con = POOL.getResource(100);
        try (final Statement statement = con.createStatement()) {
            statement.executeUpdate("insert into account values (1, 500)");
            statement.executeUpdate("insert into account values (2, 300)");
            statement.executeUpdate("insert into account values (3, 200)");
            statement.executeUpdate("insert into account values (4, 300)");
            statement.executeUpdate("insert into account values (5, 400)");
            statement.executeUpdate("insert into account values (6, 500)");
            statement.executeUpdate("insert into account values (7, 1000)");

            con.commit();
        } finally {
            POOL.returnResource(con);
        }
    }


    public static void createAndFillTestDatabase() throws SQLException, InterruptedException {
        createAccountTable();

        final Connection con = POOL.getResource(100);
        try (final Statement statement = con.createStatement()) {
            statement.executeUpdate("insert into account values (1, 100)");
            statement.executeUpdate("insert into account values (2, 200)");
            statement.executeUpdate("insert into account values (3, 300)");
            statement.executeUpdate("insert into account values (4, 400)");
            statement.executeUpdate("insert into account values (5, 500)");

            con.commit();
        } finally {
            POOL.returnResource(con);
        }
    }

    public static void cleanTestDatabase() throws SQLException, InterruptedException {
        final Connection con = POOL.getResource(100);

        try (final Statement statement = con.createStatement()) {
            statement.executeUpdate("drop table account if exists;");
        } finally {
            POOL.returnResource(con);
        }
    }

    private static void createAccountTable() throws SQLException, InterruptedException {
        final Connection con = POOL.getResource(100);

        try (final Statement statement = con.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ACCOUNT\n" +
                    "(\n" +
                    "    ID NUMBER(8) not NULL primary key,\n" +
                    "    AMOUNT FLOAT(10)\n" +
                    ");");
        } finally {
            POOL.returnResource(con);
        }
    }
}
