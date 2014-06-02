package net.digihippo.bread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Account {
    private final int id;
    private int balance = 0;
    private final Map<Integer, Integer> orders = new HashMap<Integer, Integer>();

    public Account(int id) {
        this.id = id;
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

    public void accumulateOrderQuantities(OrderQuantityAccumulator accumulator) {
        for (Integer orderQuantity : orders.values()) {
            accumulator.addOrderQuantity(orderQuantity);
        }
    }

    public void fillOrders(int quantity, final OutboundEvents events, Iterator<Account> accountIterator) {
        final Iterator<Map.Entry<Integer,Integer>> iterator = orders.entrySet().iterator();
        int remainingFillQuantity = quantity;
        while (iterator.hasNext())
        {
            final Map.Entry<Integer, Integer> orderEntry = iterator.next();
            final int orderId = orderEntry.getKey();
            final int orderQuantity = orderEntry.getValue();
            final int fillQuantity = Math.min(orderQuantity, remainingFillQuantity);
            final int remainingOrderQuantity = orderQuantity - remainingFillQuantity;
            events.orderFilled(id, orderId, fillQuantity);
            if (remainingOrderQuantity <= 0) {
                iterator.remove();
            } else {
                orderEntry.setValue(remainingOrderQuantity);
            }

            remainingFillQuantity = Math.max(0, remainingFillQuantity - orderQuantity);
        }

        fillNextAccount(events, accountIterator, remainingFillQuantity);
    }

    public static void fillNextAccount(OutboundEvents events,
                                       Iterator<Account> accountIterator,
                                       int remainingFillQuantity) {
        if (accountIterator.hasNext()) {
            accountIterator.next().fillOrders(remainingFillQuantity, events, accountIterator);
        }
    }
}
