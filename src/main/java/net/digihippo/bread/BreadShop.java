package net.digihippo.bread;

public class BreadShop {
    public static int PRICE_OF_BREAD = 12;

    private final OutboundEvents events;
    private final AccountRepository accountRepository = new AccountRepository();

    public BreadShop(OutboundEvents events) {
        this.events = events;
    }

    public void createAccount(int id) {
        Account newAccount = new Account(id);
        accountRepository.addAccount(id, newAccount);
        events.accountCreatedSuccessfully(id);
    }

    public void deposit(int accountId, int creditAmount) {
        accountRepository.deposit(accountId, creditAmount, events);
    }

    public void placeOrder(int accountId, int orderId, int amount) {
        accountRepository.placeOrder(accountId, orderId, amount, events, PRICE_OF_BREAD);
    }

    public void cancelOrder(int accountId, int orderId) {
        accountRepository.cancelOrder(accountId, orderId, events, PRICE_OF_BREAD);
    }

    public void placeWholesaleOrder() {
        final OrderQuantityAccumulator accumulator = new OrderQuantityAccumulator();
        accountRepository.accumulateOrderQuantities(accumulator);
        accumulator.placeWholesaleOrder(events);
    }

    public void onWholesaleOrder(int quantity) {
        accountRepository.onWholesaleOrder(quantity, events);
    }
}
