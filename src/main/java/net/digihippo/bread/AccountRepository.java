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

    public void deposit(int accountId, int creditAmount, OutboundEvents events) {
        if (!accounts.containsKey(accountId)) {
            events.accountNotFound(accountId);
            return;
        }
        final int newBalance = accounts.get(accountId).deposit(creditAmount);
        events.newAccountBalance(accountId, newBalance);
    }

    public void placeOrder(int accountId, int orderId, int amount, OutboundEvents events) {
        if (!accounts.containsKey(accountId)) {
            events.accountNotFound(accountId);
            return;
        }
        int cost = amount * BreadShop.PRICE_OF_BREAD;
        Account account = accounts.get(accountId);
        if (account.getBalance() >= cost) {
            account.addOrder(orderId, amount);
            int newBalance = account.deposit(-cost);
            events.orderPlaced(accountId, amount);
            events.newAccountBalance(accountId, newBalance);
        } else {
            events.orderRejected(accountId);
        }
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