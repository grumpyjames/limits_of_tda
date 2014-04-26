package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class Account {
    private final String accountName;

    private int balance = 0;
    private final Map<Integer, Integer> orders = new HashMap<Integer, Integer>();

    public Account(String accountName) {
        this.accountName = accountName;
    }

    public int getBalance() {
        return balance;
    }

    public int deposit(int creditAmount) {
        balance += creditAmount;
        return balance;
    }

    public void addOrder(int orderId, int amount) {
        orders.put(orderId, amount);
    }

    public Integer cancelOrder(int orderId) {
        return orders.remove(orderId);
    }
}
