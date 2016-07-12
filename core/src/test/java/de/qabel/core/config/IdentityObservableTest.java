package de.qabel.core.config;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;

public class IdentityObservableTest {

    @Test
    public void testIdentityObservable() throws Exception {
        final AtomicBoolean hasCalled = new AtomicBoolean();
        IdentityEquivalentTestFactory identityTest = new IdentityEquivalentTestFactory();
        final Identity identity = identityTest.create();
        identity.attach(new IdentityObserver() {
            @Override
            public void update() {
                hasCalled.set(true);
            }
        });

        identity.setAlias("new alias");
        assertTrue(hasCalled.get());
    }
}
