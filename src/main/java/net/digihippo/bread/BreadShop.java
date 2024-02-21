package net.digihippo.bread;

public class BreadShop {
    private static final int PRICE_OF_BREAD = 12;

    private final OutboundEvents events;
    private final AccountRepository accountRepository = new AccountRepository();

    public BreadShop(OutboundEvents events) {
        this.events = events;
    }

    public void createAccount(int id) {
        Account newAccount = new Account();
        accountRepository.addAccount(id, newAccount);
        events.accountCreatedSuccessfully(id);
    }

    public void deposit(int accountId, int creditAmount) {
        accountRepository.deposit(accountId, creditAmount, events);
    }

    public void placeOrder(int accountId, int orderId, int amount) {
        accountRepository.placeOrder(accountId, orderId, amount, events);
    }

    public void cancelOrder(int accountId, int orderId) {
        accountRepository.cancelOrder(accountId, orderId, events);
    }

    public void placeWholesaleOrder() {
        throw new UnsupportedOperationException("Implement me in Objective A");
    }

    public void onWholesaleOrder(int quantity) {
        throw new UnsupportedOperationException("Implement me in Objective B");
    }
}
