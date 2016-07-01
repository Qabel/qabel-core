package de.qabel.core.config;

import de.qabel.core.contacts.ContactExchangeFormats;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblInvalidFormatException;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class ContactExportImport {

    private static final ContactExchangeFormats exchangeFormats = new ContactExchangeFormats();

    public static String exportContacts(Contacts contacts) throws JSONException {
        return exchangeFormats.exportToContactsJSON(contacts.getContacts());
    }

    public static String exportContact(Contact contact) {
        return exchangeFormats.exportToContactsJSON(Collections.singleton(contact));
    }

    /**
     * Exports the {@link Contact} information as a JSON string from an {@link Identity}
     *
     * @param identity {@link Identity} to export {@link Contact} information from
     * @return {@link Contact} information as JSON string
     */
    public static String exportIdentityAsContact(Identity identity) {
        return exchangeFormats.exportToContactsJSON(identity);
    }

    /**
     * Parse a {@link Contact} from a {@link Contact} JSON string
     *
     * @param json {@link Contact} JSON string
     * @return {@link Contact} parsed from JSON string
     */
    public static Contact parseContactForIdentity(String json) throws JSONException, URISyntaxException, QblDropInvalidURL {
        try {
            return exchangeFormats.importFromContactsJSON(json).get(0);
        }catch (IndexOutOfBoundsException | QblInvalidFormatException e){
            throw new JSONException(e);
        }
    }

    /**
     * Parse {@link Contacts} from a {@link Contacts} JSON string
     *
     * @param identity {@link Identity} for setting the owner of the {@link Contact}s
     * @param json     {@link Contacts} JSON string
     * @return {@link Contacts} parsed from JSON string
     */
    public static Contacts parseContactsForIdentity(Identity identity, String json) throws JSONException, URISyntaxException, QblDropInvalidURL {
        Contacts contacts = new Contacts(identity);
        try {

            List<Contact> contactList = exchangeFormats.importFromContactsJSON(json);
            for(Contact contact : contactList){
                contacts.put(contact);
            }
            return contacts;
        } catch (QblInvalidFormatException e) {
            throw new JSONException(e);
        }
    }
}
