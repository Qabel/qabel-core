package de.qabel.core.repository;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.config.VerificationStatus;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.config.factory.IdentityBuilder;
import de.qabel.core.drop.DropURL;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository;
import de.qabel.core.repository.sqlite.SqliteIdentityRepository;
import de.qabel.core.repository.sqlite.SqlitePrefixRepository;
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

public class SqliteIdentityRepositoryTest extends AbstractSqliteRepositoryTest<SqliteIdentityRepository> {
    private IdentityBuilder identityBuilder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        identityBuilder = new IdentityBuilder(new DropUrlGenerator("http://localhost"));
        identityBuilder.withAlias("testuser");
    }

    @Override
    protected SqliteIdentityRepository createRepo(ClientDatabase clientDatabase, EntityManager em) {
        SqliteDropUrlRepository dropUrlRepository = new SqliteDropUrlRepository(clientDatabase, new DropURLHydrator());
        SqlitePrefixRepository prefixRepository = new SqlitePrefixRepository(clientDatabase);

        return new SqliteIdentityRepository(clientDatabase, em, prefixRepository, dropUrlRepository);
    }

    @Test
    public void returnsEmptyListWithoutInstances() throws Exception {
        Identities results = repo.findAll();
        assertEquals(0, results.getIdentities().size());
    }

    @Test(expected = EntityNotFoundException.class)
    public void throwsExceptionOnMissingFind() throws Exception {
        repo.find("123");
    }

    @Test
    public void findsSavedIdentities() throws Exception {
        Identity identity = identityBuilder.build();
        identity.setEmail("email");
        identity.setPhone("phone");
        repo.save(identity);
        Identity loaded = repo.find(identity.getKeyIdentifier());

        assertSame(identity, loaded);
    }

    @Test
    public void findsSavedIdentitiesFromPreviousSession() throws Exception {
        Identity identity = identityBuilder.build();
        identity.setEmail("email");
        identity.setEmailStatus(VerificationStatus.PENDING);
        identity.setPhone("phone");
        identity.setPhoneStatus(VerificationStatus.VERIFIED);
        identity.getPrefixes().add("my prefix");
        repo.save(identity);
        em.clear();

        Identity loaded = repo.find(identity.getKeyIdentifier());

        assertNotNull(loaded);
        assertEquals(identity.getKeyIdentifier(), loaded.getKeyIdentifier());
        assertTrue(Arrays.equals(
            identity.getPrimaryKeyPair().getPrivateKey(),
            loaded.getPrimaryKeyPair().getPrivateKey()
        ));
        assertEquals(identity.getAlias(), loaded.getAlias());
        assertEquals(identity.getEmail(), loaded.getEmail());
        assertEquals(identity.getEmailStatus(), loaded.getEmailStatus());
        assertEquals(identity.getPhone(), loaded.getPhone());
        assertEquals(identity.getPhoneStatus(), loaded.getPhoneStatus());
        Set<DropURL> oldUrls = identity.getDropUrls();
        Set<DropURL> newUrls = loaded.getDropUrls();
        assertTrue(
            "DropUrls not loaded correctly: " + oldUrls + " != " + newUrls,
            Arrays.equals(oldUrls.toArray(), newUrls.toArray())
        );
        assertTrue(Arrays.equals(identity.getPrefixes().toArray(), loaded.getPrefixes().toArray()));
    }

    @Test
    public void findsSavedIdentitiesCollection() throws Exception {
        Identity identity = identityBuilder.build();
        identity.setEmail("email");
        identity.setPhone("phone");
        repo.save(identity);
        Identities loaded = repo.findAll();

        assertNotNull(loaded);
        assertEquals(1, loaded.getIdentities().size());
        assertSame(identity, loaded.getIdentities().toArray()[0]);
    }

    @Test
    public void alwaysLoadsTheSameInstance() throws Exception {
        Identity identity = identityBuilder.build();
        repo.save(identity);
        em.clear();

        Identity instance1 = repo.find(identity.getKeyIdentifier());
        Identity instance2 = repo.find(identity.getKeyIdentifier());
        assertSame(instance1, instance2);
    }

    @Test
    public void findsSavedIdentitiesCollectionFromPreviousSession() throws Exception {
        Identity identity = identityBuilder.build();
        identity.setEmail("email");
        identity.setPhone("phone");
        repo.save(identity);
        em.clear();

        Identities loaded = repo.findAll();

        assertNotNull(loaded);
        assertEquals(1, loaded.getIdentities().size());
        Identity loadedIdentity = loaded.getByKeyIdentifier(identity.getKeyIdentifier());

        assertNotNull(loadedIdentity);
        assertEquals(identity.getKeyIdentifier(), loadedIdentity.getKeyIdentifier());
        assertTrue(Arrays.equals(
            identity.getPrimaryKeyPair().getPrivateKey(),
            loadedIdentity.getPrimaryKeyPair().getPrivateKey()
        ));
        assertEquals(identity.getAlias(), loadedIdentity.getAlias());
        assertEquals(identity.getEmail(), loadedIdentity.getEmail());
        assertEquals(identity.getPhone(), loadedIdentity.getPhone());
        assertTrue(Arrays.equals(identity.getDropUrls().toArray(), loadedIdentity.getDropUrls().toArray()));
    }
}
