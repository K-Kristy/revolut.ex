package org.kuplkris.accounts.entity;

import java.util.Objects;

public class Account {

    private static final double EPSILON = 0.001;
    private final Long accountId;
    private final double amount;

    public Account(final long accountId, final double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public long getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }

        final Account account = (Account) o;

        return account.accountId.equals(this.accountId) && equalsDouble(account.amount, this.amount);
    }

    @Override
    public String toString() {
        return "accountId = " + accountId + " amount = " + amount;
    }

    private static boolean equalsDouble(final double a, final double b) {
        return Math.abs(a - b) < EPSILON;
    }
}
