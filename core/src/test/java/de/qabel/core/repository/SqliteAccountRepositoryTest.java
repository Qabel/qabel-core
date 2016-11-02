package de.qabel.core.repository;

import de.qabel.core.config.Account;
import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.SqliteAccountRepository;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class SqliteAccountRepositoryTest extends AbstractSqliteRepositoryTest<SqliteAccountRepository> {

    @Override
    protected SqliteAccountRepository createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        return new SqliteAccountRepository(clientDatabase, em);
    }

    @Test
    public void findsSavedAccount() throws Exception {
        Account account = new Account("p", "u", "a");
        repo.save(account);

        List<Account> accounts = repo.findAll();

        assertEquals(1, accounts.size());
        assertSame(account, accounts.get(0));
    }

    @Test
    public void findsUncachedAccounts() throws Exception {
        Account account = new Account("p", "u", "a");
        account.setToken("token");
        repo.save(account);
        em.clear();

        List<Account> accounts = repo.findAll();
        assertEquals(1, accounts.size());
        Account loaded = accounts.get(0);
        assertEquals("p", loaded.getProvider());
        assertEquals("u", loaded.getUser());
        assertEquals("a", loaded.getAuth());
        assertEquals("token", loaded.getToken());
        assertEquals(account.getId(), loaded.getId());
    }

    @Test
    public void providesInternalReferenceApi() throws Exception {
        Account account = new Account("p", "u", "a");
        repo.save(account);

        assertSame(account, repo.find(String.valueOf(account.getId())));
    }

    @Test
    public void updatesExistingAccounts() throws Exception {
        Account account = new Account("p", "u", "a");
        repo.save(account);
        account.setAuth("777");
        account.setToken("888");
        repo.save(account);

        assertEquals(1, repo.findAll().size());
        assertEquals("777", repo.findAll().get(0).getAuth());
        assertEquals("888", repo.findAll().get(0).getToken());
    }
}
