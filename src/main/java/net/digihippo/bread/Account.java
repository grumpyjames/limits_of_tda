package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class Account {
    private int balance = 0;
    private final Map<Integer, Integer> orders = new HashMap<Integer, Integer>();

    public void deposit(int creditAmount, int accountId, OutboundEvents events) {
        balance += creditAmount;
        events.newAccountBalance(accountId, balance);
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

        this.deposit(cancelledQuantity * BreadShop.PRICE_OF_BREAD, accountId, events);
        events.orderCancelled(accountId, orderId);
    }

    public void placeOrder(int accountId, int orderId, int amount, OutboundEvents events) {
        int cost = amount * BreadShop.PRICE_OF_BREAD;
        if (balance >= cost) {
            this.addOrder(orderId, amount);
            this.deposit(-cost, accountId, events);
            events.orderPlaced(accountId, amount);
        } else {
            events.orderRejected(accountId);
        }

    }
}
