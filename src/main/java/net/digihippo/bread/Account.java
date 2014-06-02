package net.digihippo.bread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Account {
    private final int id;
    private final Map<Integer, Integer> orders = new HashMap<Integer, Integer>();
    private int balance = 0;

    Account(int id) {
        this.id = id;
    }

    void deposit(int creditAmount) {
        balance += creditAmount;
    }

    void addOrder(int orderId, int amount) {
        orders.put(orderId, amount);
    }

    void accumulateOrderQuantities(OrderQuantityAccumulator accumulator) {
        for (Integer orderQuantity : orders.values()) {
            accumulator.addOrderQuantity(orderQuantity);
        }
    }

    void fillOrders(int quantity, final OutboundEvents events, Iterator<Account> accountIterator) {
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

    static void fillNextAccount(OutboundEvents events,
                                       Iterator<Account> accountIterator,
                                       int remainingFillQuantity) {
        if (accountIterator.hasNext()) {
            accountIterator.next().fillOrders(remainingFillQuantity, events, accountIterator);
        }
    }

    void placeOrder(OutboundEvents events, int accountId, int orderId, int amount, int priceOfBread) {
        int cost = amount * priceOfBread;
        if (balance >= cost) {
            addOrder(orderId, amount);
            deposit(-cost);
            events.orderPlaced(accountId, amount);
            events.newAccountBalance(accountId, balance);
        } else {
            events.orderRejected(accountId);
        }
    }

    void deposit(OutboundEvents outboundEvents, int accountId, int creditAmount) {
        deposit(creditAmount);
        outboundEvents.newAccountBalance(accountId, balance);
    }

    void cancelOrder(int accountId, int orderId, OutboundEvents outboundEvents, int priceOfBread) {
        Integer cancelledQuantity = orders.remove(orderId);
        if (cancelledQuantity == null)
        {
            outboundEvents.orderNotFound(accountId, orderId);
            return;
        }

        deposit(outboundEvents, accountId, cancelledQuantity * priceOfBread);
        outboundEvents.orderCancelled(accountId, orderId);
    }
}
