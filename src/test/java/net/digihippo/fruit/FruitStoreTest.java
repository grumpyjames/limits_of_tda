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
        String forename = "Geoff";
        String surname = "Smith";
        expectAccountCreationSuccess(accountId);

        fruitStore.createAccount(accountId, forename, surname);
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
