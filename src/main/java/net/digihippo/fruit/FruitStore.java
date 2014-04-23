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
            events.newAccountBalance(accountId, creditAmount);
        } else {
            events.accountNotFound(accountId);
        }

    }
}
