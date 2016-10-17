package de.qabel.core.repository.sqlite.hydrator;

import de.qabel.core.config.Identity;
import de.qabel.core.config.Prefix;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.config.factory.IdentityFactory;
import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.Hydrator;
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository;
import de.qabel.core.repository.sqlite.SqlitePrefixRepository;
import org.spongycastle.util.encoders.Hex;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class IdentityHydrator extends AbstractHydrator<Identity> implements Hydrator<Identity> {
    private final SqliteDropUrlRepository dropUrlRepository;
    private final IdentityFactory identityFactory;
    private final EntityManager entityManager;
    private final SqlitePrefixRepository prefixRepository;

    public IdentityHydrator(
        IdentityFactory identityFactory,
        EntityManager entityManager,
        SqliteDropUrlRepository dropUrlRepository,
        SqlitePrefixRepository prefixRepository
    ) {
        this.identityFactory = identityFactory;
        this.entityManager = entityManager;
        this.dropUrlRepository = dropUrlRepository;
        this.prefixRepository = prefixRepository;
    }

    @Override
    public String[] getFields(String... alias) {
        String i = alias[0] + ".";
        String c = alias[1] + ".";
        return new String[]{i+"id", i+"privateKey", c+"id", c+"alias", c+"email", c+"phone"};
    }

    @Override
    protected String[] getFields() {
        return new String[]{"id", "privateKey", "id", "alias", "email", "phone"};
    }

    @Override
    public Identity hydrateOne(ResultSet resultSet) throws SQLException {
        Collection<DropURL> dropUrls = new HashSet<>();
        int i = 1;
        int id = resultSet.getInt(i++);
        if (entityManager.contains(Identity.class, id)) {
            return entityManager.get(Identity.class, id);
        }
        byte[] privateKey = Hex.decode(resultSet.getString(i++));
        int contactId = resultSet.getInt(i++);
        String alias = resultSet.getString(i++);
        String email = resultSet.getString(i++);
        String phone = resultSet.getString(i++);

        Identity identity = identityFactory.createIdentity(new QblECKeyPair(privateKey), dropUrls, alias);
        identity.setId(id);
        identity.setEmail(email);
        identity.setPhone(phone);
        try {
            for (DropURL url : dropUrlRepository.findAll(contactId)) {
                identity.addDrop(url);
            }
            for (Prefix prefix : prefixRepository.findAll(identity)) {
                identity.getPrefixes().add(prefix);
            }
        } catch (PersistenceException e) {
            throw new SQLException("failed to load drop urls for identity", e);
        }
        entityManager.put(Identity.class, identity);
        return identity;
    }

    @Override
    public void recognize(Identity identity) {
        entityManager.put(Identity.class, identity);
    }
}
