package de.qabel.core.repository.sqlite.hydrator;

import org.spongycastle.util.encoders.Hex;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;
import de.qabel.core.config.factory.ContactFactory;
import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository;

public class ContactHydrator extends AbstractHydrator<Contact> {
    private EntityManager em;
    private ContactFactory contactFactory;
    private SqliteDropUrlRepository dropUrlRepository;

    public ContactHydrator(EntityManager em, ContactFactory contactFactory, SqliteDropUrlRepository dropUrlRepository) {
        this.em = em;
        this.contactFactory = contactFactory;
        this.dropUrlRepository = dropUrlRepository;
    }

    @Override
    public String[] getFields() {
        return new String[]{"id", "publicKey", "alias", "phone", "email"};
    }

    @Override
    public Contact hydrateOne(ResultSet resultSet) throws SQLException {
        int column = 1;
        int id = resultSet.getInt(column++);
        if (em.contains(Contact.class, id)) {
            return em.get(Contact.class, id);
        }

        String publicKeyAsHex = resultSet.getString(column++);
        String alias = resultSet.getString(column++);
        String phone = resultSet.getString(column++);
        String email = resultSet.getString(column++);

        QblECPublicKey publicKey = new QblECPublicKey(Hex.decode(publicKeyAsHex));

        Contact contact = contactFactory.createContact(publicKey, new LinkedList<DropURL>(), alias);
        contact.setId(id);
        contact.setPhone(phone);
        contact.setEmail(email);

        try {
            for (DropURL url : dropUrlRepository.findAll(contact)) {
                contact.addDrop(url);
            }
        } catch (PersistenceException e) {
            throw new SQLException("Failed to load DropUrls for contact: " + e.getMessage(), e);
        }

        recognize(contact);
        return contact;
    }

    @Override
    public void recognize(Contact instance) {
        em.put(Contact.class, instance);
    }
}
