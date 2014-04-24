package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class AccountRepository {
    final Map<Integer, Account> accounts = new HashMap<Integer, Account>();

    public AccountRepository() {
    }

    void addAccount(int id, Account newAccount) {
        accounts.put(id, newAccount);
    }

    Account getAccount(int accountId) {
        return accounts.get(accountId);
    }
}