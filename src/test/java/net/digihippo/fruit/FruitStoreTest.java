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
