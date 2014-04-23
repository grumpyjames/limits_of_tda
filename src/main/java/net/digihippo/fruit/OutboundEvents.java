package net.digihippo.fruit;

public interface OutboundEvents {
    void accountCreatedSuccessfully(int accountId);

    void newAccountBalance(int accountId, int newBalanceAmount);

    void accountNotFound(int accountId);
}
