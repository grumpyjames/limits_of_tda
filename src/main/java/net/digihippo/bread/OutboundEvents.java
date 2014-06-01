package net.digihippo.bread;

public interface OutboundEvents {
    void accountCreatedSuccessfully(int accountId);

    void newAccountBalance(int accountId, int newBalanceAmount);

    void accountNotFound(int accountId);

    void orderPlaced(int accountId, int amount);

    void orderRejected(int accountId);

    void orderCancelled(int accountId, int orderId);

    void orderNotFound(int accountId, int orderId);

    // For Objective A
    void onWholesaleOrder(int quantity);
}
