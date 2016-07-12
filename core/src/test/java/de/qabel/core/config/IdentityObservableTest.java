package de.qabel.core.config;


import org.junit.Test;

public class IdentityObservableTest {

    @Test
    public void testIdentityObservable() throws Exception {
        IdentityEquivalentTestFactory identityTest = new IdentityEquivalentTestFactory();
        final Identity identity = identityTest.create();
        identity.attach(new IdentityObserver() {
            @Override
            public void update() {
                System.out.println("Identity changed.");
            }
        });

        identity.setAlias("new alias");
    }
}
