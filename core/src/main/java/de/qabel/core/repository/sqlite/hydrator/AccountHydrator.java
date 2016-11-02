package de.qabel.core.repository.sqlite.hydrator;

import de.qabel.core.config.Account;
import de.qabel.core.config.factory.AccountFactory;
import de.qabel.core.repository.EntityManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountHydrator extends AbstractHydrator<Account> {
    private final EntityManager em;
    private final AccountFactory accountFactory;

    public AccountHydrator(EntityManager em, AccountFactory accountFactory) {
        this.em = em;
        this.accountFactory = accountFactory;
    }

    @Override
    protected String[] getFields() {
        return new String[]{"id", "provider", "user", "auth", "token"};
    }

    @Override
    public Account hydrateOne(ResultSet resultSet) throws SQLException {
        int i = 1;
        int id = resultSet.getInt(i++);

        if (em.contains(Account.class, id)) {
            return em.get(Account.class, id);
        }

        String provider = resultSet.getString(i++);
        String user = resultSet.getString(i++);
        String auth = resultSet.getString(i++);
        String token = resultSet.getString(i++);

        Account account = accountFactory.createAccount(provider, user, auth);
        account.setId(id);
        account.setToken(token);
        return account;
    }

    @Override
    public void recognize(Account instance) {
        em.put(Account.class, instance);
    }
}
