package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class Account {
    private int balance = 0;
    private final Map<Integer, Integer> orders = new HashMap<Integer, Integer>();

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

    public void cancelOrder(int accountId, int orderId, OutboundEvents events) {

        Integer cancelledQuantity = orders.remove(orderId);

        if (cancelledQuantity == null) {
            events.orderNotFound(accountId, orderId);
            return;
        }

        int newBalance = this.deposit(cancelledQuantity * BreadShop.PRICE_OF_BREAD);
        events.orderCancelled(accountId, orderId);
        events.newAccountBalance(accountId, newBalance);

    }
}
