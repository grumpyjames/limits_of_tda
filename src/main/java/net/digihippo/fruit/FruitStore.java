package net.digihippo.fruit;

import java.util.HashMap;
import java.util.Map;

public class FruitStore {
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
}
