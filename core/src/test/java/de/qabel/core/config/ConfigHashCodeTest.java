package de.qabel.core.config;

import de.qabel.core.ExtendedHashCodeMethodTester;
import de.qabel.core.crypto.QblECPublicKeyTestFactory;
import de.qabel.core.crypto.QblEcPairTestFactory;
import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class ConfigHashCodeTest {
    @Test
    public void accountHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .ignoreProperty("serialVersionUID")
            .ignoreProperty("auth")
            .ignoreProperty("token")
            .build();
        tester.testHashCodeMethod(new AccountEquivalentTestFactory(), config);
    }

    @Test
    public void contactHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .overrideFactory("ecPublicKey", new QblECPublicKeyTestFactory())
            .overrideFactory("contactOwner", new IdentityTestFactory())
            .ignoreProperty("contactOwnerKeyId") // depends on contactOwner, therefore not significant
            .ignoreProperty("serialVersionUID")
            .ignoreProperty("alias")
            .ignoreProperty("email")
            .ignoreProperty("phone")
            .ignoreProperty("status")
            .ignoreProperty("ignored")
            .ignoreProperty("nickName")
            .build();
        tester.testHashCodeMethod(new ContactEquivalentTestFactory(), config);
    }

    @Test
    public void dropServerHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .ignoreProperty("serialVersionUID")
            .overrideFactory("uri", new UriTestFactory())
            .build();
        tester.testHashCodeMethod(new DropServerEquivalentTestFactory(), config);
    }

    @Test
    public void identityHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .overrideFactory("ecKeyPair", new QblEcPairTestFactory())
            .overrideFactory("drops", new DropUrlListTestFactory())
            .ignoreProperty("serialVersionUID")
            .ignoreProperty("alias")
            .ignoreProperty("emailStatus")
            .ignoreProperty("email")
            .ignoreProperty("phone")
            .ignoreProperty("phoneStatus")
            .ignoreProperty("observers")
            .ignoreProperty("prefixes")
            .ignoreProperty("uploadEnabled")
            .build();
        tester.testHashCodeMethod(new IdentityEquivalentTestFactory(), config);
    }


    @Test
    public void accountsHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        tester.testHashCodeMethod(new AccountsEquivalentTestFactory());
    }

    @Test
    public void contactsHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .ignoreProperty("serialVersionUID")
            .ignoreProperty("entities")
            .overrideFactory("identity", new IdentityTestFactory())
            .build();
        tester.testHashCodeMethod(new ContactsEquivalentTestFactory(), config);
    }

    @Test
    public void dropServersHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        tester.testHashCodeMethod(new DropServersEquivalentTestFactory());
    }

    @Test
    public void identitiesHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .ignoreProperty("serialVersionUID")
            .build();
        tester.testHashCodeMethod(new IdentitiesEquivalentTestFactory(), config);
    }


    @Test
    public void localSettingsHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .ignoreProperty("serialVersionUID")
            .ignoreProperty("dateFormat")
            .build();
        tester.testHashCodeMethod(new LocalSettingsEquivalentTestFactory(), config);
    }

    @Test
    public void syncedSettingsHashCodeTest() {
        ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
        Configuration config = new ConfigurationBuilder()
            .overrideFactory("accounts", new AccountsTestFactory())
            .overrideFactory("contacts", new ContactsSetTestFactory())
            .overrideFactory("dropServers", new DropServersTestFactory())
            .overrideFactory("identities", new IdentitiesTestFactory())
            .build();
        tester.testHashCodeMethod(new SyncedSettingsEquivalentTestFactory(), config);
    }
}
