package net.digihippo.fruit;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

public class FruitStoreTest {
    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();
    private final OutboundEvents events = mockery.mock(OutboundEvents.class);
    private final FruitStore fruitStore = new FruitStore(events);
    private int idSequence = 0;

    @Test
    public void create_an_account() {
        int accountId = nextId();
        expectAccountCreationSuccess(accountId);

        fruitStore.createAccount(accountId, "Geoff", "Smith");
    }

    @Test
    public void deposit_some_money() {
        int accountId = nextId();
        createAccount(accountId, "Amanda", "Price");

        expectNewBalance(accountId, 300);
        fruitStore.deposit(accountId, 300);
    }

    @Test
    public void reject_deposits_for_nonexistent_accounts() {
        expectAccountNotFound(-5);

        fruitStore.deposit(-5, 4000);
    }

    @Test
    public void deposits_add_up() {
        int accountId = nextId();
        createAccountWithBalance(accountId, "Peter", "Parker", 300);

        expectNewBalance(accountId, 600);
        fruitStore.deposit(accountId, 300);
    }

    @Test
    public void place_an_order_if_there_is_enough_money() {
        int accountId = nextId();
        createAccountWithBalance(accountId, "Penelope", "Pitstop", 500);

        expectOrderPlaced(accountId, 40);
        expectNewBalance(accountId, 500 - (40 * FruitStore.PRICE_PER_FRUIT));
        fruitStore.placeOrder(accountId, 40);
    }

    @Test
    public void cannot_place_order_for_nonexistent_account() {
        expectAccountNotFound(-5);
        fruitStore.placeOrder(-5, 40);
    }

    @Test
    public void cannot_place_an_order_for_more_than_account_can_afford() {
        int accountId = nextId();
        createAccountWithBalance(accountId, "Dick", "Dastardly", 500);

        // 42 * 12 = 504
        expectOrderRejected(accountId);
        fruitStore.placeOrder(accountId, 42);
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
        fruitStore.deposit(accountId, initialBalance);
    }

    private void expectAccountNotFound(final int accountId) {
        mockery.checking(new Expectations()
        {{
            oneOf(events).accountNotFound(accountId);
        }});
    }

    private void createAccount(int accountId, String forename, String surname) {
        expectAccountCreationSuccess(accountId);

        fruitStore.createAccount(accountId, forename, surname);
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

    private int nextId() {
        return idSequence++;
    }
}
