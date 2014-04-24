package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class Account {
    private final int id;
    private final String forename;
    private final String surname;

    private int balance = 0;
    private final Map<Integer, Integer> orders = new HashMap<Integer, Integer>();

    public Account(int id, String forename, String surname) {
        this.id = id;
        this.forename = forename;
        this.surname = surname;
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
