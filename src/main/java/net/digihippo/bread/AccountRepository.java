package net.digihippo.bread;

import java.util.HashMap;
import java.util.Map;

public class AccountRepository {
    private final Map<Integer, Account> accounts = new HashMap<Integer, Account>();

    void addAccount(int id, Account newAccount) {
        accounts.put(id, newAccount);
    }

    void accumulateOrderQuantities(OrderQuantityAccumulator accumulator) {
        for (Account account: accounts.values()) {
            account.accumulateOrderQuantities(accumulator);
        }
    }

    void onWholesaleOrder(int quantity, OutboundEvents events) {
        Account.fillNextAccount(events, accounts.values().iterator(), quantity);
    }

    void deposit(int accountId, int creditAmount, OutboundEvents outboundEvents) {
        Account account = accounts.get(accountId);
        if (account != null) {
            account.deposit(outboundEvents, accountId, creditAmount);
        } else {
            outboundEvents.accountNotFound(accountId);
        }
    }

    void placeOrder(int accountId, int orderId, int amount, OutboundEvents events, int priceOfBread) {
        Account account = accounts.get(accountId);
        if (account != null) {
            account.placeOrder(events, accountId, orderId, amount, priceOfBread);
        } else {
            events.accountNotFound(accountId);
        }
    }

    void cancelOrder(int accountId, int orderId, OutboundEvents outboundEvents, int priceOfBread) {
        Account account = accounts.get(accountId);
        if (account != null) {
            account.cancelOrder(accountId, orderId, outboundEvents, priceOfBread);
        } else {
            outboundEvents.accountNotFound(accountId);
        }
    }
}