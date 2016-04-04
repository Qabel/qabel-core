package de.qabel.core.config;

import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class EncryptedPersistenceTest {
    private static final char[] encryptionPassword = "qabel".toCharArray();
    private static final String DB_NAME = "EncryptedPersistenceTest.sqlite";
    private static final int PBKDF2_ROUNDS = 1; // Low value only for testing
    EncryptedPersistence<String> persistence;

    @Before
    public void setUp() throws QblInvalidEncryptionKeyException {
        persistence = new SQLiteEncryptedPersistence(DB_NAME, encryptionPassword, PBKDF2_ROUNDS);
    }

    @After
    public void tearDown() throws Exception {
        File persistenceTestDB = new File(DB_NAME);
        if (persistenceTestDB.exists()) {
            persistenceTestDB.delete();
        }
    }

    @Test(expected = QblInvalidEncryptionKeyException.class)
    public void openWithWrongPasswordTest() throws QblInvalidEncryptionKeyException {
        persistence = new SQLiteEncryptedPersistence(DB_NAME, "wrongPassword".toCharArray(), PBKDF2_ROUNDS);
    }

    @Test
    public void getNotPersistedEntityTest() {
        Assert.assertNull(persistence.getEntity("1", PersistenceTestObject.class));
    }

    @Test
    public void getEntitiesTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        PersistenceTestObject pto2 = new PersistenceTestObject("pto2");

        Assert.assertTrue(persistence.persistEntity(pto));
        Assert.assertTrue(persistence.persistEntity(pto2));

        List<PersistenceTestObject> objects = persistence.getEntities(PersistenceTestObject.class);
        Assert.assertEquals(2, objects.size());
        Assert.assertTrue(objects.contains(pto));
        Assert.assertTrue(objects.contains(pto2));
    }

    @Test
    public void getEntitiesEmptyTest() {
        List<PersistenceTestObject> objects = persistence.getEntities(PersistenceTestObject.class);
        Assert.assertEquals(0, objects.size());
    }

    @Test
    public void updateEntityTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        pto.data = "changed";

        Assert.assertTrue(persistence.persistEntity(pto));
        Assert.assertTrue(persistence.updateEntity(pto));

        PersistenceTestObject receivedPto = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertEquals(pto, receivedPto);
    }

    @Test
    public void updateOrPersistEntityTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");

        Assert.assertTrue(persistence.updateOrPersistEntity(pto));
        PersistenceTestObject receivedPto = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertEquals(pto, receivedPto);

        pto.data = "changed";
        Assert.assertTrue(persistence.updateOrPersistEntity(pto));
        receivedPto = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertEquals(pto, receivedPto);
    }

    @Test
    public void updateNotStoredEntityTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        Assert.assertFalse(persistence.updateEntity(pto));
    }

    @Test
    public void persistenceRoundTripTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        persistence.persistEntity(pto);

        // Assure that pto has been persisted
        PersistenceTestObject receivedPto = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertEquals(pto, receivedPto);

        Assert.assertTrue(persistence.removeEntity(pto.getPersistenceID(), PersistenceTestObject.class));

        PersistenceTestObject receivedPto2 = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertNull(receivedPto2);
    }

    @Test
    public void dropTableTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        persistence.persistEntity(pto);

        Assert.assertTrue(persistence.dropTable(PersistenceTestObject.class));
        Assert.assertNull(persistence.getEntity(pto.getPersistenceID(), PersistenceTestObject.class));
    }

    @Test
    public void dropNotExistingTableTest() {
        Assert.assertFalse(persistence.dropTable(PersistenceTestObject.class));
    }

    @Test
    public void changeMasterKeyTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        persistence.persistEntity(pto);

        Assert.assertTrue(persistence.changePassword(encryptionPassword, "qabel2".toCharArray()));

        PersistenceTestObject receivedPto = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertEquals(pto, receivedPto);
    }

    @Test
    public void changeMasterKeyFailTest() {
        PersistenceTestObject pto = new PersistenceTestObject("pto");
        persistence.persistEntity(pto);

        Assert.assertFalse(persistence.changePassword("wrongPassword".toCharArray(), "qabel2".toCharArray()));

        PersistenceTestObject receivedPto = persistence.getEntity(pto.getPersistenceID(),
            PersistenceTestObject.class);
        Assert.assertEquals(pto, receivedPto);
    }

    public static class PersistenceTestObject extends Persistable {
        private static final long serialVersionUID = -9721591389456L;
        public String data;

        public PersistenceTestObject(String data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PersistenceTestObject that = (PersistenceTestObject) o;

            return !(data != null ? !data.equals(that.data) : that.data != null);
        }

        @Override
        public int hashCode() {
            return data != null ? data.hashCode() : 0;
        }
    }

    public static class PersistenceTestObject2 extends Persistable {
        private static final long serialVersionUID = -832569264920L;
        public String data;

        public PersistenceTestObject2(String data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PersistenceTestObject that = (PersistenceTestObject) o;

            return !(data != null ? !data.equals(that.data) : that.data != null);
        }

        @Override
        public int hashCode() {
            return data != null ? data.hashCode() : 0;
        }
    }
}
