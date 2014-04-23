package net.digihippo.fruit;

import java.util.HashMap;
import java.util.Map;

public class FruitStore {
    public static int PRICE_PER_FRUIT = 12;

    private final OutboundEvents events;
    private final Map<Integer, Integer> balances = new HashMap<Integer, Integer>();

    public FruitStore(OutboundEvents events) {
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

    public void placeOrder(int accountId, int amount) {
        Integer balance = balances.get(accountId);
        if (balance != null) {
            int cost = amount * PRICE_PER_FRUIT;
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
