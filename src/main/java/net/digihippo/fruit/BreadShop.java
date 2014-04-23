package net.digihippo.fruit;

import java.util.HashMap;
import java.util.Map;

public class BreadShop {
    public static int PRICE_OF_BREAD = 12;

    private final OutboundEvents events;
    private final Map<Integer, Integer> balances = new HashMap<Integer, Integer>();

    public BreadShop(OutboundEvents events) {
        this.events = events;
    }

    public void createAccount(int id, String forename, String surname) {
        balances.put(id, 0);
        events.accountCreatedSuccessfully(id);
    }

    public void deposit(int accountId, int creditAmount) {
        Integer balance = balances.get(accountId);
        if (balance != null) {
            int newBalance = creditAmount + balance;
            balances.put(accountId, newBalance);
            events.newAccountBalance(accountId, newBalance);
        } else {
            events.accountNotFound(accountId);
        }
    }

    public void placeOrder(int accountId, int orderId, int amount) {
        Integer balance = balances.get(accountId);
        if (balance != null) {
            int cost = amount * PRICE_OF_BREAD;
            if (balance > cost) {
                events.orderPlaced(accountId, amount);
                deposit(accountId, -cost);
            } else {
                events.orderRejected(accountId);
            }
        } else {
            events.accountNotFound(accountId);
        }
    }
}
