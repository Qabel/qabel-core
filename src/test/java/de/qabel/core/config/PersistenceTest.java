package de.qabel.core.config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class PersistenceTest {
	private final static char[] encryptionPassword = "qabel".toCharArray();
	private final static String DB_NAME = "persistenceTest.sqlite";
	Persistence<String> persistence;

	@Before
	public void setUp() {
		Persistence.setPassword(encryptionPassword);
		persistence = new SQLitePersistence(DB_NAME);
	}

	@After
	public void tearDown() throws Exception {
		File persistenceTestDB = new File(DB_NAME);
		if(persistenceTestDB.exists()) {
			persistenceTestDB.delete();
		}
	}

	@Test
	public void getNotPersistedEntityTest() {
		Assert.assertNull(persistence.getEntity("1", PersistenceTestObject.class));
	}

	@Test
	public void getEntitiesTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		PersistenceTestObject pto2 = new PersistenceTestObject("pto2");

		Assert.assertTrue(persistence.persistEntity("1", pto));
		Assert.assertTrue(persistence.persistEntity("2", pto2));

		List<Object> objects = persistence.getEntities(PersistenceTestObject.class);
		System.out.println(objects.size());
		Assert.assertTrue(objects.size() == 2);
		Assert.assertTrue(objects.contains(pto));
		Assert.assertTrue(objects.contains(pto2));
	}

	@Test
	public void getEntitiesEmptyTest() {
		List<Object> objects = persistence.getEntities(PersistenceTestObject.class);
		Assert.assertEquals(0, objects.size());
	}

	@Test (expected=IllegalArgumentException.class)
	public void noOverwriteTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		PersistenceTestObject pto2 = new PersistenceTestObject("pto2");

		Assert.assertTrue(persistence.persistEntity("1", pto));
		persistence.persistEntity("1", pto2);
	}

	@Test
	public void updateEntityTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		PersistenceTestObject pto2 = new PersistenceTestObject("pto2");

		Assert.assertTrue(persistence.persistEntity("1", pto));
		Assert.assertTrue(persistence.updateEntity("1", pto2));

		PersistenceTestObject receivedPto = (PersistenceTestObject) persistence.getEntity("1", PersistenceTestObject.class);
		Assert.assertNotEquals(pto, receivedPto);
		Assert.assertEquals(pto2, receivedPto);
	}

	@Test
	public void updateEntityWrongClassTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		PersistenceTestObject2 pto2 = new PersistenceTestObject2("pto2");

		Assert.assertTrue(persistence.persistEntity("1", pto));
		Assert.assertFalse(persistence.updateEntity("1", pto2));
	}

	@Test
	public void updateNotStoredEntityTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		Assert.assertFalse(persistence.updateEntity("1", pto));
	}

	@Test
	public void persistenceRoundTripTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		persistence.persistEntity("1", pto);

		// Assure that pto has been persisted
		PersistenceTestObject receivedPto = (PersistenceTestObject) persistence.getEntity("1", PersistenceTestObject.class);
		Assert.assertEquals(pto, receivedPto);

		Assert.assertTrue(persistence.removeEntity("1", PersistenceTestObject.class));

		PersistenceTestObject receivedPto2 = (PersistenceTestObject) persistence.getEntity("1", PersistenceTestObject.class);
		Assert.assertNull(receivedPto2);
	}

	@Test
	public void dropTableTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		persistence.persistEntity("1", pto);

		Assert.assertTrue(persistence.dropTable(PersistenceTestObject.class));
		Assert.assertNull(persistence.getEntity("1", PersistenceTestObject.class));
	}

	@Test
	public void dropNotExistingTableTest() {
		Assert.assertFalse(persistence.dropTable(PersistenceTestObject.class));
	}

	@Test
	public void changeMasterKeyTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		persistence.persistEntity("1", pto);

		Assert.assertTrue(persistence.changePassword(encryptionPassword, "qabel2".toCharArray()));

		PersistenceTestObject receivedPto = (PersistenceTestObject) persistence.getEntity("1", PersistenceTestObject.class);
		Assert.assertEquals(pto, receivedPto);
	}

	@Test
	public void changeMasterKeyFailTest() {
		PersistenceTestObject pto = new PersistenceTestObject("pto");
		persistence.persistEntity("1", pto);

		Assert.assertFalse(persistence.changePassword("wrongPassword".toCharArray(), "qabel2".toCharArray()));

		PersistenceTestObject receivedPto = (PersistenceTestObject) persistence.getEntity("1", PersistenceTestObject.class);
		Assert.assertEquals(pto, receivedPto);
	}

	static public class PersistenceTestObject implements Serializable {
		private static final long serialVersionUID = -9721591389456L;
		public String data;

		public PersistenceTestObject(String data) {
			this.data = data;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			if(o == null || getClass() != o.getClass()) {
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

	static public class PersistenceTestObject2 implements Serializable {
		private static final long serialVersionUID = -832569264920L;
		public String data;

		public PersistenceTestObject2(String data) {
			this.data = data;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			if(o == null || getClass() != o.getClass()) {
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
