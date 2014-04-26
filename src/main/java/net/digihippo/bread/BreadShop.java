package net.digihippo.bread;

public class BreadShop {
    public static int PRICE_OF_BREAD = 12;

    private final OutboundEvents events;
    private final AccountRepository accountRepository = new AccountRepository();

    public BreadShop(OutboundEvents events) {
        this.events = events;
    }

    public void createAccount(int id, String accountName) {
        Account newAccount = new Account(accountName);
        accountRepository.addAccount(id, newAccount);
        events.accountCreatedSuccessfully(id);
    }

    public void deposit(int accountId, int creditAmount) {
        Account account = accountRepository.getAccount(accountId);
        if (account != null) {
            final int newBalance = account.deposit(creditAmount);
            events.newAccountBalance(accountId, newBalance);
        } else {
            events.accountNotFound(accountId);
        }
    }

    public void placeOrder(int accountId, int orderId, int amount) {
        Account account = accountRepository.getAccount(accountId);
        if (account != null) {
            int cost = amount * PRICE_OF_BREAD;
            if (account.getBalance() > cost) {
                account.addOrder(orderId, amount);
                int newBalance = account.deposit(-cost);
                events.orderPlaced(accountId, amount);
                events.newAccountBalance(accountId, newBalance);
            } else {
                events.orderRejected(accountId);
            }
        } else {
            events.accountNotFound(accountId);
        }
    }

    public void cancelOrder(int accountId, int orderId) {
        Account account = accountRepository.getAccount(accountId);
        if (account == null)
        {
            events.accountNotFound(accountId);
            return;
        }

        Integer cancelledQuantity = account.cancelOrder(orderId);
        if (cancelledQuantity == null)
        {
            events.orderNotFound(accountId, orderId);
            return;
        }

        int newBalance = account.deposit(cancelledQuantity * PRICE_OF_BREAD);
        events.orderCancelled(accountId, orderId);
        events.newAccountBalance(accountId, newBalance);
    }
}
