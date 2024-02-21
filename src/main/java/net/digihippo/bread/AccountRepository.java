package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class AccountRepository {
    private final Map<Integer, Account> accounts = new HashMap<Integer, Account>();

    public AccountRepository() {
    }

    void addAccount(int id, Account newAccount) {
        accounts.put(id, newAccount);
    }

    public void deposit(int accountId, int creditAmount, OutboundEvents events) {
        if (!accounts.containsKey(accountId)) {
            events.accountNotFound(accountId);
            return;
        }
        accounts.get(accountId).deposit(creditAmount, accountId, events);
    }

    public void placeOrder(int accountId, int orderId, int amount, OutboundEvents events) {
        if (!accounts.containsKey(accountId)) {
            events.accountNotFound(accountId);
            return;
        }
        Account account = accounts.get(accountId);
        account.placeOrder(accountId, orderId, amount, events);
    }

    public void cancelOrder(int accountId, int orderId, OutboundEvents events) {
        if (!accounts.containsKey(accountId)) {
            events.accountNotFound(accountId);
            return;
        }

        Account account = accounts.get(accountId);
        account.cancelOrder(accountId, orderId, events);
    }
}