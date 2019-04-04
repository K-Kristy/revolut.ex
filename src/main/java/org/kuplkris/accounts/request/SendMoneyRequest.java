package org.kuplkris.accounts.request;


public class SendMoneyRequest {

    private long fromAccountId;
    private long toAccountId;
    private double sum;

    public long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(final long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(final double sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "fromAccountId = " + fromAccountId +
                " sum = " + sum +
                " toAccountId = " + toAccountId;
    }

    public long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(final long toAccountId) {
        this.toAccountId = toAccountId;
    }
}
