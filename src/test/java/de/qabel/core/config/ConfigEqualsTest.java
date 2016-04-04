package de.qabel.core.config;

import de.qabel.core.crypto.QblECPublicKeyTestFactory;
import de.qabel.core.crypto.QblEcPairTestFactory;
import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;
import org.meanbean.test.EqualsMethodTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class ConfigEqualsTest {

    @Test
    public void accountEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        tester.testEqualsMethod(new AccountEquivalentTestFactory());
    }

    @Test
    public void accountsEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        tester.testEqualsMethod(Accounts.class);

        AccountTestFactory accountFactory = new AccountTestFactory();

        Account a1 = accountFactory.create();
        Account a2 = accountFactory.create();
        Account c1 = accountFactory.create();

        Accounts a = new Accounts();
        Accounts b = new Accounts();
        Accounts c = new Accounts();

        a.put(a1);
        a.put(a2);

        b.put(a1);
        b.put(a2);

        c.put(a1);
        c.put(c1);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
    }

    @Test
    public void contactsEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        tester.testEqualsMethod(new ContactsEquivalentTestFactory());

        ContactTestFactory contactFactory = new ContactTestFactory();

        Contact a1 = contactFactory.create();
        Contact a2 = contactFactory.create();
        Contact c1 = contactFactory.create();

        Identity i = new IdentityTestFactory().create();

        Contacts a = new Contacts(i);
        Contacts b = new Contacts(i);
        Contacts c = new Contacts(i);

        a.put(a1);
        a.put(a2);

        b.put(a1);
        b.put(a2);

        c.put(a1);
        c.put(c1);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
    }

    @Test
    public void dropServersEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        tester.testEqualsMethod(DropServers.class);

        DropServerTestFactory dropServerFactory = new DropServerTestFactory();

        DropServer a1 = dropServerFactory.create();
        DropServer a2 = dropServerFactory.create();
        DropServer c1 = dropServerFactory.create();

        DropServers a = new DropServers();
        DropServers b = new DropServers();
        DropServers c = new DropServers();

        a.put(a1);
        a.put(a2);

        b.put(a1);
        b.put(a2);

        c.put(a1);
        c.put(c1);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
    }

    @Test
    public void identitiesEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        tester.testEqualsMethod(Identities.class);

        IdentityTestFactory identityFactory = new IdentityTestFactory();

        Identity a1 = identityFactory.create();
        Identity a2 = identityFactory.create();
        Identity c1 = identityFactory.create();

        Identities a = new Identities();
        Identities b = new Identities();
        Identities c = new Identities();

        a.put(a1);
        a.put(a2);

        b.put(a1);
        b.put(a2);

        c.put(a1);
        c.put(c1);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
    }

    @Test
    public void syncedSettingsEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        Configuration config = new ConfigurationBuilder()
            .overrideFactory("accounts", new AccountsTestFactory())
            .overrideFactory("contacts", new ContactsTestFactory())
            .overrideFactory("dropServers", new DropServersTestFactory())
            .overrideFactory("identities", new IdentitiesTestFactory())
            .iterations(5)
            .build();
        tester.testEqualsMethod(new SyncedSettingsEquivalentTestFactory(), config);
    }

    @Test
    public void localSettingsEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        tester.testEqualsMethod(new LocalSettingsEquivalentTestFactory());
    }

    @Test
    public void identityEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        Configuration config = new ConfigurationBuilder()
            .overrideFactory("drops", new DropUrlListTestFactory())
            .overrideFactory("primaryKeyPair", new QblEcPairTestFactory())
            .ignoreProperty("email")
            .ignoreProperty("phone")
            .iterations(10)
            .build();
        tester.testEqualsMethod(new IdentityEquivalentTestFactory(), config);
    }

    @Test
    public void dropServerEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        Configuration config = new ConfigurationBuilder()
            .overrideFactory("uri", new UriTestFactory())
            .build();
        tester.testEqualsMethod(new DropServerEquivalentTestFactory(), config);
    }

    @Test
    public void contactEqualsTest() {
        EqualsMethodTester tester = new EqualsMethodTester();
        Configuration config = new ConfigurationBuilder()
            .iterations(10)
            .overrideFactory("ecPublicKey", new QblECPublicKeyTestFactory())
            .ignoreProperty("email")
            .ignoreProperty("phone")
            .build();
        tester.testEqualsMethod(new ContactEquivalentTestFactory(), config);
    }

}
