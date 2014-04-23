package net.digihippo.fruit;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
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

        breadShop.createAccount(accountId, "Geoff", "Smith");
    }

    @Test
    public void deposit_some_money() {
        createAccount(accountId, "Amanda", "Price");

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
        createAccountWithBalance(accountId, "Peter", "Parker", 300);

        expectNewBalance(accountId, 600);
        breadShop.deposit(accountId, 300);
    }

    @Test
    public void place_an_order_if_there_is_enough_money() {
        createAccountWithBalance(accountId, "Penelope", "Pitstop", 500);

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
        createAccountWithBalance(accountId, "Dick", "Dastardly", 500);

        // 42 * 12 = 504
        expectOrderRejected(accountId);
        breadShop.placeOrder(accountId, orderId, 42);
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

    private void createAccountWithBalance(int accountId, String forename, String surname, int initialBalance) {
        createAccount(accountId, forename, surname);

        expectNewBalance(accountId, initialBalance);
        breadShop.deposit(accountId, initialBalance);
    }

    private void expectAccountNotFound(final int accountId) {
        mockery.checking(new Expectations()
        {{
            oneOf(events).accountNotFound(accountId);
        }});
    }

    private void createAccount(int accountId, String forename, String surname) {
        expectAccountCreationSuccess(accountId);

        breadShop.createAccount(accountId, forename, surname);
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
}
