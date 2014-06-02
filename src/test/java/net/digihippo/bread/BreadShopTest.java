package net.digihippo.bread;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class BreadShopTest {
    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();
    private final OutboundEvents events = mockery.mock(OutboundEvents.class);
    private final BreadShop breadShop = new BreadShop(events);

    private final int accountId = 11;
    private final int orderId = 7;

    @Test
    public void create_an_account() {
        expectAccountCreationSuccess(accountId);

        breadShop.createAccount(accountId, "Geoff Smith");
    }

    @Test
    public void deposit_some_money() {
        createAccount(accountId, "Amanda Price");

        expectNewBalance(accountId, 300);
        breadShop.deposit(accountId, 300);
    }

    @Test
    public void reject_deposits_for_nonexistent_accounts() {
        expectAccountNotFound(-5);

        breadShop.deposit(-5, 4000);
    }

    @Test
    public void deposits_add_up() {
        createAccountWithBalance(accountId, "Peter Parker", 300);

        expectNewBalance(accountId, 600);
        breadShop.deposit(accountId, 300);
    }

    @Test
    public void place_an_order_succeeds_if_there_is_enough_money() {
        createAccountWithBalance(accountId, "Penelope Pitstop", 500);

        expectOrderPlaced(accountId, 40);
        expectNewBalance(accountId, 500 - (40 * BreadShop.PRICE_OF_BREAD));
        breadShop.placeOrder(accountId, orderId, 40);
    }

    @Test
    public void cannot_place_order_for_nonexistent_account() {
        expectAccountNotFound(-5);
        breadShop.placeOrder(-5, orderId, 40);
    }

    @Test
    public void cannot_place_an_order_for_more_than_account_can_afford() {
        createAccountWithBalance(accountId, "Dick Dastardly", 500);

        // 42 * 12 = 504
        expectOrderRejected(accountId);
        breadShop.placeOrder(accountId, orderId, 42);
    }

    @Test
    public void cancel_an_order_by_id() {
        int balance = 500;
        createAccountWithBalance(accountId, "Penelope Pitstop", balance);

        int amount = 40;
        placeOrder(accountId, orderId, amount, balance);

        expectOrderCancelled(accountId, orderId);
        expectNewBalance(accountId, balance);

        breadShop.cancelOrder(accountId, orderId);
    }

    @Test
    public void cannot_cancel_an_order_for_nonexistent_account() {
        expectAccountNotFound(-5);

        breadShop.cancelOrder(-5, orderId);
    }

    @Test
    public void cannot_cancel_a_nonexistent_order() {
        createAccount(accountId, "Leona Latimer");

        expectOrderNotFound(-5);
        breadShop.cancelOrder(accountId, -5);
    }

    @Test
    public void cancelling_an_allows_balance_to_be_reused() {
        int balance = 500;
        createAccountWithBalance(accountId, "Penelope Pitstop", balance);

        int amount = 40;
        placeOrder(accountId, orderId, amount, balance);
        cancelOrder(accountId, orderId, balance);

        // it's entirely possible that the balance in the resulting event doesn't match the internal
        // state of the system, so we ensure the balance has really been restored
        // by trying to place a new order with it.
        expectOrderPlaced(accountId, amount);
        expectNewBalance(accountId, balance - (amount * BreadShop.PRICE_OF_BREAD));
        breadShop.placeOrder(accountId, orderId, amount);
    }

    @Test
    @Ignore("Objective A")
    public void an_empty_shop_places_an_empty_wholesale_order() {
        expectWholesaleOrder(0);

        breadShop.placeWholesaleOrder();
    }

    @Test
    @Ignore("Objective A")
    public void wholesale_orders_are_made_for_a_sum_of_the_quantities_of_outstanding_orders() {
        expectWholesaleOrder(40 + 55 + 61);

        createAccountAndPlaceOrder(accountId, orderId, 40);
        createAccountAndPlaceOrder(accountId + 1, orderId, 55);
        createAccountAndPlaceOrder(accountId + 2, orderId, 61);
    }

    @Test
    @Ignore("Objective B")
    public void arrival_of_wholesale_order_trigger_fills_of_a_single_outstanding_order() {
        int quantity = 40;
        createAccountAndPlaceOrder(accountId, orderId, quantity);

        expectOrderFilled(accountId, orderId, quantity);
        breadShop.onWholesaleOrder(quantity);
    }

    @Test
    @Ignore("Objective B")
    public void wholesale_order_quantities_might_only_fill_an_outstanding_order_partially() {
        int quantity = 40;
        createAccountAndPlaceOrder(accountId, orderId, quantity);

        int wholesaleOrderQuantity = quantity / 2;
        expectOrderFilled(accountId, orderId, wholesaleOrderQuantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);
    }

    @Test
    @Ignore("Objective B")
    public void an_order_can_be_filled_by_two_consecutive_wholesale_orders() {
        int quantity = 40;
        createAccountAndPlaceOrder(accountId, orderId, quantity);

        int wholesaleOrderQuantity = quantity / 2;
        expectOrderFilled(accountId, orderId, wholesaleOrderQuantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);

        expectOrderFilled(accountId, orderId, wholesaleOrderQuantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);
    }

    @Test
    @Ignore("Objective B")
    public void orders_do_not_overfill() {
        int quantity = 40;
        int wholesaleOrderQuantity = 42;
        createAccountAndPlaceOrder(accountId, orderId, quantity);

        expectOrderFilled(accountId, orderId, quantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);
    }

    @Test
    @Ignore("Objective B")
    public void orders_across_different_accounts_are_filled() {
        int quantityOne = 40;
        int accountIdOne = accountId;
        int quantityTwo = 55;
        int accountIdTwo = accountId + 1;
        createAccountAndPlaceOrder(accountIdOne, orderId, quantityOne);
        createAccountAndPlaceOrder(accountIdTwo, orderId, quantityTwo);

        expectOrderFilled(accountIdOne, orderId, quantityOne);
        expectOrderFilled(accountIdTwo, orderId, quantityTwo);

        breadShop.onWholesaleOrder(quantityOne + quantityTwo);
    }

    @Test
    @Ignore("Objective B")
    public void orders_fill_in_a_consistent_order_across_different_accounts() {
        int quantityOne = 40;
        int accountIdOne = accountId;
        int quantityTwo = 55;
        int accountIdTwo = accountId + 1;
        createAccountAndPlaceOrder(accountIdOne, orderId, quantityOne);
        createAccountAndPlaceOrder(accountIdTwo, orderId, quantityTwo);

        expectOrderFilled(accountIdOne, orderId, quantityOne);
        int secondFillQuantity = 8;
        expectOrderFilled(accountIdTwo, orderId, secondFillQuantity);

        breadShop.onWholesaleOrder(quantityOne + secondFillQuantity);
    }

    @Test
    @Ignore("Objective B")
    public void orders_fill_in_a_consistent_order_across_orders_in_the_same_account() {
        int quantityOne = 40;
        int accountIdOne = accountId;
        int balance = 40 * 2 * BreadShop.PRICE_OF_BREAD;
        createAccountWithBalance(accountIdOne, "Penelope Pitstop", balance);
        placeOrder(accountIdOne, orderId, quantityOne, balance);
        placeOrder(accountIdOne, orderId + 1, quantityOne, balance / 2);

        expectOrderFilled(accountIdOne, orderId, quantityOne);
        int secondFillQuantity = 8;
        expectOrderFilled(accountIdOne, orderId + 1, secondFillQuantity);

        breadShop.onWholesaleOrder(quantityOne + secondFillQuantity);
    }

    private void expectOrderFilled(final int accountId, final int orderId, final int quantity) {
        mockery.checking(new Expectations() {{
            oneOf(events).orderFilled(accountId, orderId, quantity);
        }});
    }

    private void cancelOrder(int accountId, int orderId, int expectedBalanceAfterCancel) {
        expectOrderCancelled(accountId, orderId);
        expectNewBalance(accountId, expectedBalanceAfterCancel);

        breadShop.cancelOrder(accountId, this.orderId);
    }

    private void expectOrderNotFound(final int orderId) {
        mockery.checking(new Expectations() {{
            oneOf(events).orderNotFound(accountId, orderId);
        }});
    }

    private void expectOrderCancelled(final int accountId, final int orderId) {
        mockery.checking(new Expectations(){{
            oneOf(events).orderCancelled(accountId, orderId);
        }});
    }

    private void placeOrder(int accountId, int orderId, int amount, int balanceBefore) {
        expectOrderPlaced(accountId, amount);
        expectNewBalance(accountId, balanceBefore - (amount * BreadShop.PRICE_OF_BREAD));
        breadShop.placeOrder(accountId, orderId, amount);
    }

    private void expectOrderRejected(final int accountId) {
        mockery.checking(new Expectations() {{
            oneOf(events).orderRejected(accountId);
        }});
    }

    private void expectOrderPlaced(final int accountId, final int amount) {
        mockery.checking(new Expectations() {{
            oneOf(events).orderPlaced(accountId, amount);
        }});
    }

    private void createAccountWithBalance(int accountId, String accountName, int initialBalance) {
        createAccount(accountId, accountName);

        expectNewBalance(accountId, initialBalance);
        breadShop.deposit(accountId, initialBalance);
    }

    private void expectAccountNotFound(final int accountId) {
        mockery.checking(new Expectations() {{
            oneOf(events).accountNotFound(accountId);
        }});
    }

    private void createAccount(int accountId, String accountName) {
        expectAccountCreationSuccess(accountId);

        breadShop.createAccount(accountId, accountName);
    }

    private void expectNewBalance(final int accountId, final int newBalanceAmount) {
        mockery.checking(new Expectations() {{
            oneOf(events).newAccountBalance(accountId, newBalanceAmount);
        }});
    }

    private void expectAccountCreationSuccess(final int accountId) {
        mockery.checking(new Expectations() {{
            oneOf(events).accountCreatedSuccessfully(accountId);
        }});
    }

    private void createAccountAndPlaceOrder(int accountId, int orderId, int amount) {
        int balance = 40 * BreadShop.PRICE_OF_BREAD;
        createAccountWithBalance(accountId, "Penelope Pitstop", balance);
        placeOrder(accountId, orderId, amount, balance);
    }

    private void expectWholesaleOrder(final int quantity) {
        mockery.checking(new Expectations() {{
            oneOf(events).onWholesaleOrder(quantity);
        }});
    }
}
