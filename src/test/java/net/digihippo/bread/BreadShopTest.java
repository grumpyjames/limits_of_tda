package net.digihippo.bread;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

public class BreadShopTest {
    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();
    private final OutboundEvents events = mockery.mock(OutboundEvents.class);
    private final BreadShop breadShop = new BreadShop(events);

    private final int accountIdOne = 1;
    private final int accountIdTwo = 2;
    private final int orderIdOne = 1;
    private final int orderIdTwo = 2;

    @Test
    public void create_an_account() {
        expectAccountCreationSuccess(accountIdOne);

        breadShop.createAccount(accountIdOne);
    }

    @Test
    public void deposit_some_money() {
        createAccount(accountIdOne);

        int depositAmount = 300;
        expectNewBalance(accountIdOne, depositAmount);
        breadShop.deposit(accountIdOne, depositAmount);
    }

    @Test
    public void reject_deposits_for_nonexistent_accounts() {
        int nonExistentAccountId = -5;
        expectAccountNotFound(nonExistentAccountId);

        breadShop.deposit(nonExistentAccountId, 4000);
    }

    @Test
    public void deposits_add_up() {
        createAccountWithBalance(accountIdOne, 300);

        expectNewBalance(accountIdOne, 600);
        breadShop.deposit(accountIdOne, 300);
    }

    @Test
    public void place_an_order_succeeds_if_there_is_enough_money() {
        createAccountWithBalance(accountIdOne, 500);

        expectOrderPlaced(accountIdOne, 40);
        expectNewBalance(accountIdOne, 500 - (cost(40)));
        breadShop.placeOrder(accountIdOne, orderIdOne, 40);
    }

    @Test
    public void cannot_place_order_for_nonexistent_account() {
        expectAccountNotFound(-5);
        breadShop.placeOrder(-5, orderIdOne, 40);
    }

    @Test
    public void cannot_place_an_order_for_more_than_account_can_afford() {
        createAccountWithBalance(accountIdOne, 500);

        // 42 * 12 = 504
        expectOrderRejected(accountIdOne);
        breadShop.placeOrder(accountIdOne, orderIdOne, 42);
    }

    @Test
    public void cancel_an_order_by_id() {
        int balance = 500;
        createAccountWithBalance(accountIdOne, balance);

        int amount = 40;
        placeOrder(accountIdOne, orderIdOne, amount, balance);

        expectOrderCancelled(accountIdOne, orderIdOne);
        expectNewBalance(accountIdOne, balance);

        breadShop.cancelOrder(accountIdOne, orderIdOne);
    }

    @Test
    public void cannot_cancel_an_order_for_nonexistent_account() {
        expectAccountNotFound(-5);

        breadShop.cancelOrder(-5, orderIdOne);
    }

    @Test
    public void cannot_cancel_a_nonexistent_order() {
        createAccount(accountIdOne);

        expectOrderNotFound(-5);
        breadShop.cancelOrder(accountIdOne, -5);
    }

    @Test
    public void cancelling_an_allows_balance_to_be_reused() {
        int balance = 500;
        createAccountWithBalance(accountIdOne, balance);

        int amount = 40;
        placeOrder(accountIdOne, orderIdOne, amount, balance);
        cancelOrder(accountIdOne, orderIdOne, balance);

        // it's entirely possible that the balance in the resulting event doesn't match the internal
        // state of the system, so we ensure the balance has really been restored
        // by trying to place a new order with it.
        expectOrderPlaced(accountIdOne, amount);
        expectNewBalance(accountIdOne, balance - (cost(amount)));
        breadShop.placeOrder(accountIdOne, orderIdTwo, amount);
    }

    @Test
    public void an_empty_shop_places_an_empty_wholesale_order() {
        expectWholesaleOrder(0);

        breadShop.placeWholesaleOrder();
    }

    @Test
    public void wholesale_orders_are_made_for_a_sum_of_the_quantities_of_outstanding_orders_in_one_account() {
        expectWholesaleOrder(40 + 55);

        int balance = cost(40 + 55);
        createAccountWithBalance(accountIdOne, balance);
        placeOrder(accountIdOne, orderIdOne, 40, balance);
        placeOrder(accountIdOne, orderIdTwo, 55, balance - cost(40));

        breadShop.placeWholesaleOrder();
    }

    @Test
    public void wholesale_orders_are_made_for_a_sum_of_the_quantities_of_outstanding_orders() {
        expectWholesaleOrder(40 + 55);

        createAccountAndPlaceOrder(accountIdOne, orderIdOne, 40);
        createAccountAndPlaceOrder(accountIdTwo, orderIdTwo, 55);

        breadShop.placeWholesaleOrder();
    }

    @Test
    public void arrival_of_wholesale_order_trigger_fills_of_a_single_outstanding_order() {
        int quantity = 40;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

        expectOrderFilled(accountIdOne, orderIdOne, quantity);
        breadShop.onWholesaleOrder(quantity);
    }

    @Test
    public void wholesale_order_quantities_might_only_fill_an_outstanding_order_partially() {
        int quantity = 40;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

        int wholesaleOrderQuantity = quantity / 2;
        expectOrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);
    }

    @Test
    public void an_order_can_be_filled_by_two_consecutive_wholesale_orders() {
        int quantity = 40;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

        int wholesaleOrderQuantity = quantity / 2;
        expectOrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);

        expectOrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);
    }

    @Test
    public void orders_do_not_overfill() {
        int quantity = 40;
        int wholesaleOrderQuantity = 42;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

        expectOrderFilled(accountIdOne, orderIdOne, quantity);
        breadShop.onWholesaleOrder(wholesaleOrderQuantity);
    }

    @Test
    public void fully_filled_orders_are_removed_and_therefore_cannot_be_cancelled()
    {
        int quantity = 40;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

        expectOrderFilled(accountIdOne, orderIdOne, quantity);
        breadShop.onWholesaleOrder(quantity);

        expectOrderNotFound(orderIdOne);
        breadShop.cancelOrder(accountIdOne, orderIdOne);
    }

    @Test
    public void orders_do_not_overfill_across_two_wholesale_orders() {
        int quantity = 40;
        int wholesaleOrderQuantityOne = 21;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

        expectOrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantityOne);
        breadShop.onWholesaleOrder(wholesaleOrderQuantityOne);

        // This will fill the remaining quantity...
        int wholesaleOrderQuantityTwo = 33;
        expectOrderFilled(accountIdOne, orderIdOne, quantity - wholesaleOrderQuantityOne);
        breadShop.onWholesaleOrder(wholesaleOrderQuantityTwo);
    }

    @Test
    public void orders_across_different_accounts_are_filled() {
        int quantityOne = 40;
        int quantityTwo = 55;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantityOne);
        createAccountAndPlaceOrder(accountIdTwo, orderIdTwo, quantityTwo);

        expectOrderFilled(accountIdOne, orderIdOne, quantityOne);
        expectOrderFilled(accountIdTwo, orderIdTwo, quantityTwo);

        breadShop.onWholesaleOrder(quantityOne + quantityTwo);
    }

    @Test
    public void orders_fill_in_a_consistent_order_across_different_accounts() {
        int quantityOne = 40;
        int quantityTwo = 55;
        createAccountAndPlaceOrder(accountIdOne, orderIdOne, quantityOne);
        createAccountAndPlaceOrder(accountIdTwo, orderIdTwo, quantityTwo);

        expectOrderFilled(accountIdOne, orderIdOne, quantityOne);
        int secondFillQuantity = 8;
        expectOrderFilled(accountIdTwo, orderIdTwo, secondFillQuantity);

        breadShop.onWholesaleOrder(quantityOne + secondFillQuantity);
    }

    @Test
    public void orders_fill_in_a_consistent_order_across_orders_in_the_same_account() {
        int quantityOne = 40;
        int quantityTwo = 50;
        int balance = cost(quantityOne) + cost(quantityTwo);
        createAccountWithBalance(accountIdOne, balance);
        placeOrder(accountIdOne, orderIdOne, quantityOne, balance);
        placeOrder(accountIdOne, orderIdTwo, quantityTwo, balance - cost(quantityOne));

        expectOrderFilled(accountIdOne, orderIdOne, quantityOne);
        int secondFillQuantity = 8;
        expectOrderFilled(accountIdOne, orderIdTwo, secondFillQuantity);

        breadShop.onWholesaleOrder(quantityOne + secondFillQuantity);
    }

    private int cost(int quantityOne) {
        return quantityOne * BreadShop.PRICE_OF_BREAD;
    }

    private void expectOrderFilled(final int accountId, final int orderId, final int quantity) {
        mockery.checking(new Expectations() {{
            oneOf(events).orderFilled(accountId, orderId, quantity);
        }});
    }

    private void cancelOrder(int accountId, int orderId, int expectedBalanceAfterCancel) {
        expectOrderCancelled(accountId, orderId);
        expectNewBalance(accountId, expectedBalanceAfterCancel);

        breadShop.cancelOrder(accountId, this.orderIdOne);
    }

    private void expectOrderNotFound(final int orderId) {
        mockery.checking(new Expectations() {{
            oneOf(events).orderNotFound(accountIdOne, orderId);
        }});
    }

    private void expectOrderCancelled(final int accountId, final int orderId) {
        mockery.checking(new Expectations(){{
            oneOf(events).orderCancelled(accountId, orderId);
        }});
    }

    private void placeOrder(int accountId, int orderId, int amount, int balanceBefore) {
        expectOrderPlaced(accountId, amount);
        expectNewBalance(accountId, balanceBefore - (cost(amount)));
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

    private void createAccountWithBalance(int accountId, int initialBalance) {
        createAccount(accountId);

        expectNewBalance(accountId, initialBalance);
        breadShop.deposit(accountId, initialBalance);
    }

    private void expectAccountNotFound(final int accountId) {
        mockery.checking(new Expectations() {{
            oneOf(events).accountNotFound(accountId);
        }});
    }

    private void createAccount(int accountId) {
        expectAccountCreationSuccess(accountId);

        breadShop.createAccount(accountId);
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
        int balance = cost(amount);
        createAccountWithBalance(accountId, balance);
        placeOrder(accountId, orderId, amount, balance);
    }

    private void expectWholesaleOrder(final int quantity) {
        mockery.checking(new Expectations() {{
            oneOf(events).onWholesaleOrder(quantity);
        }});
    }
}
