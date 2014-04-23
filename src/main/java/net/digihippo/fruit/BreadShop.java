package net.digihippo.fruit;

import java.util.HashMap;
import java.util.Map;

public class BreadShop {
    public static int PRICE_OF_BREAD = 12;

    private final OutboundEvents events;
    private final Map<Integer, Account> accounts = new HashMap<Integer, Account>();

    public BreadShop(OutboundEvents events) {
        this.events = events;
    }

    public void createAccount(int id, String forename, String surname) {
        accounts.put(id, new Account(id, forename, surname));
        events.accountCreatedSuccessfully(id);
    }

    public void deposit(int accountId, int creditAmount) {
        Account account = accounts.get(accountId);
        if (account != null) {
            final int newBalance = account.deposit(creditAmount);
            events.newAccountBalance(accountId, newBalance);
        } else {
            events.accountNotFound(accountId);
        }
    }

    public void placeOrder(int accountId, int orderId, int amount) {
        Account account = accounts.get(accountId);
        if (account != null) {
            int cost = amount * PRICE_OF_BREAD;
            if (account.getBalance() > cost) {
                account.addOrder(orderId, amount);
                int newBalance = account.deposit(-cost);
                events.orderPlaced(accountId, amount);
                events.newAccountBalance(accountId, newBalance);
            } else {
                events.orderRejected(accountId);
            }
        } else {
            events.accountNotFound(accountId);
        }
    }

    public void cancelOrder(int accountId, int orderId) {
        Account account = accounts.get(accountId);
        Integer orderQuantity = account.cancelOrder(orderId);

        int newBalance = account.deposit(orderQuantity * PRICE_OF_BREAD);
        events.orderCancelled(accountId, orderId);
        events.newAccountBalance(accountId, newBalance);
    }
}
