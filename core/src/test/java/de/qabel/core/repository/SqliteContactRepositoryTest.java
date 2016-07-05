package de.qabel.core.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;
import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.SqliteContactRepository;
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository;
import de.qabel.core.repository.sqlite.SqliteIdentityRepository;
import de.qabel.core.config.factory.DefaultContactFactory;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.sqlite.hydrator.ContactHydrator;
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class SqliteContactRepositoryTest extends AbstractSqliteRepositoryTest<SqliteContactRepository> {
    private Identity identity;
    private Identity otherIdentity;
    private Contact contact;
    private Contact otherContact;
    private QblECPublicKey pubKey;
    private SqliteIdentityRepository identityRepository;
    private DropUrlGenerator dropUrlGenerator;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        identity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("tester").build();
        otherIdentity = new IdentityBuilder(new DropUrlGenerator("http://localhost")).withAlias("other i").build();
        pubKey = new QblECPublicKey("test".getBytes());
        contact = new Contact("testcontact", new LinkedList<DropURL>(), pubKey);
        QblECPublicKey otherPubKey = new QblECPublicKey("test2".getBytes());
        otherContact = new Contact("other contact", new LinkedList<DropURL>(), otherPubKey);
        identityRepository = new SqliteIdentityRepository(
            clientDatabase, em
        );
        identityRepository.save(identity);
        identityRepository.save(otherIdentity);
        dropUrlGenerator = new DropUrlGenerator("http://localhost");
    }

    @Override
    protected SqliteContactRepository createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        SqliteDropUrlRepository dropUrlRepository = new SqliteDropUrlRepository(clientDatabase, new DropURLHydrator());
        return new SqliteContactRepository(
            clientDatabase,
            new ContactHydrator(em, new DefaultContactFactory(), dropUrlRepository),
            dropUrlRepository
        );
    }

    @Test(expected = EntityNotFoundException.class)
    public void throwsExceptionWhenNotFound() throws Exception {
        repo.findByKeyId(identity, pubKey.getReadableKeyIdentifier());
    }

    @Test
    public void findsSavedContact() throws Exception {
        repo.save(contact, identity);
        Contact loaded = repo.findByKeyId(identity, contact.getKeyIdentifier());
        assertSame(loaded, contact);
    }

    @Test
    public void loadsUncachedContact() throws Exception {
        contact.setPhone("01234567890");
        contact.setEmail("test@test.de");
        repo.save(contact, identity);
        em.clear();

        Contact loaded = repo.findByKeyId(identity, contact.getKeyIdentifier());

        assertEquals(contact.getKeyIdentifier(), loaded.getKeyIdentifier());
        assertEquals(contact.getAlias(), loaded.getAlias());
        assertEquals("01234567890", loaded.getPhone());
        assertEquals("test@test.de", loaded.getEmail());
    }

    @Test
    public void alwaysLoadsSameInstance() throws Exception {
        repo.save(contact, identity);
        Contact instance1 = repo.findByKeyId(identity, contact.getKeyIdentifier());
        Contact instance2 = repo.findByKeyId(identity, contact.getKeyIdentifier());
        assertSame(instance1, instance2);
    }

    @Test
    public void persistsDropUrls() throws Exception {
        contact.addDrop(dropUrlGenerator.generateUrl());
        repo.save(contact, identity);
        em.clear();

        Contact loaded = repo.findByKeyId(identity, contact.getKeyIdentifier());
        compareDropUrls(loaded);
    }

    public void compareDropUrls(Contact loaded) {
        Set<DropURL> dropUrls = contact.getDropUrls();
        List<DropURL> originalUrls = new LinkedList<>(dropUrls);
        dropUrls = loaded.getDropUrls();
        List<DropURL> loadedUrls = new LinkedList<>(dropUrls);
        Collections.sort(originalUrls, new Comparator<DropURL>() {
            @Override
            public int compare(DropURL o1, DropURL o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        Collections.sort(loadedUrls, new Comparator<DropURL>() {
            @Override
            public int compare(DropURL o1, DropURL o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        assertTrue(
            "DropUrls not persisted/loaded: " + loadedUrls + " != " + originalUrls,
            Arrays.equals(originalUrls.toArray(), loadedUrls.toArray())
        );
    }

    @Test
    public void updatesEntries() throws Exception {
        contact.addDrop(dropUrlGenerator.generateUrl());
        repo.save(contact, identity);

        contact.setAlias("new alias");
        contact.setEmail("new mail");
        contact.setPhone("666");
        contact.addDrop(dropUrlGenerator.generateUrl());
        repo.save(contact, identity);
        em.clear();

        Contact loaded = repo.findByKeyId(identity, contact.getKeyIdentifier());

        assertEquals(contact.getId(), loaded.getId());
        assertEquals(contact.getAlias(), loaded.getAlias());
        assertEquals(contact.getEmail(), loaded.getEmail());
        assertEquals(contact.getPhone(), loaded.getPhone());
        compareDropUrls(loaded);
    }

    @Test
    public void providesEmptyContactListByDefault() throws Exception {
        Contacts contacts = repo.find(identity);
        assertEquals(0, contacts.getContacts().size());
        assertSame(identity, contacts.getIdentity());
    }

    @Test
    public void findsMatchingContact() throws Exception {
        repo.save(contact, identity);
        Contacts contacts = repo.find(identity);
        assertEquals(1, contacts.getContacts().size());
        assertSame(contact, contacts.getContacts().toArray()[0]);
    }

    @Test
    public void ignoresNotMatchingContacts() throws Exception {
        repo.save(contact, identity);
        repo.save(otherContact, otherIdentity);
        Contacts contacts = repo.find(otherIdentity);
        assertEquals(1, contacts.getContacts().size());
        assertSame(otherContact, contacts.getContacts().toArray()[0]);
    }

    @Test
    public void deletesContact() throws Exception {
        repo.save(contact, identity);
        repo.delete(contact, identity);

        try {
            repo.findByKeyId(identity, contact.getKeyIdentifier());
            fail("entity was not deleted");
        } catch (EntityNotFoundException ignored) {}
    }

    @Test
    public void deletesTheCorrelatedContactOnly() throws Exception {
        repo.save(contact, identity);
        repo.save(contact, otherIdentity);
        repo.delete(contact, identity);

        try {
            repo.findByKeyId(identity, contact.getKeyIdentifier());
            fail("connection from contact to identity was not deleted");
        } catch (EntityNotFoundException ignored){}

        Contact loaded = repo.findByKeyId(otherIdentity, contact.getKeyIdentifier());
        assertSame(contact, loaded);
    }

    @Test
    public void reAddedContactKeepsSameInstance() throws Exception {
        repo.save(contact, identity);
        repo.delete(contact, identity);
        repo.save(contact, identity);

        Contact loaded = repo.findByKeyId(identity, contact.getKeyIdentifier());
        assertSame(contact, loaded);
    }

    @Test
    public void addsRelationshipIfContactIsAlreadyPresent() throws Exception {
        repo.save(contact, identity);

        Contact newImport = new Contact(contact.getAlias(), contact.getDropUrls(), contact.getEcPublicKey());
        repo.save(newImport, otherIdentity);
        repo.findByKeyId(otherIdentity, contact.getKeyIdentifier());
        repo.findByKeyId(identity, contact.getKeyIdentifier());
    }

    @Test
    public void multipleContactsArePossible() throws Exception {
        repo.save(contact, identity);
        repo.save(otherContact, identity);

        Contacts contacts = repo.find(identity);
        assertThat(contacts.getContacts(), hasSize(2));
    }
}
