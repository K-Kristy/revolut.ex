package org.kuplkris.accounts.dao;

@SuppressWarnings({"EmptyClass", "WeakerAccess"})
public class DatabaseConstants {

    @SuppressWarnings({"UtilityClassCanBeEnum", "WeakerAccess", "PublicInnerClass"})
    public static final class AccountColumns {
        public static final String ID = "id";
        public static final String AMOUNT = "amount";

        private AccountColumns() {
        }
    }

    @SuppressWarnings({"UtilityClassCanBeEnum", "PublicInnerClass"})
    public static final class AccountQuery {
        public static final String SELECT_FOR_UPDATE_QUERY = "select * from account where id = ? for update";
        public static final String SELECT_QUERY = "select * from ACCOUNT where id = ?";

        private AccountQuery() {
        }
    }


}
