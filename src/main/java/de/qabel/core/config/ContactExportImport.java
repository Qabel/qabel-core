package de.qabel.core.config;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

public class ContactExportImport {

    private static final String KEY_ALIAS = "alias";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PUBLIC_KEY = "public_key";
    private static final String KEY_DROP_URLS = "drop_urls";
    private static final String KEY_CONTACTS = "contacts";

    public static String exportContacts(Contacts contacts) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonContacts = new JSONArray();
        for (Contact contact : contacts.getContacts()) {
            jsonContacts.put(getJSONfromContact(contact));
        }
        jsonObject.put(KEY_CONTACTS, jsonContacts);
        return jsonObject.toString();
    }

    public static String exportContact(Contact contact) {

        return getJSONfromContact(contact).toString();
    }

    private static JSONObject getJSONfromContact(Contact contact) {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonDropUrls = new JSONArray();
        try {
            jsonObject.put(KEY_ALIAS, contact.getAlias());
            jsonObject.put(KEY_EMAIL, contact.getEmail());
            jsonObject.put(KEY_PHONE, contact.getPhone());
            jsonObject.put(KEY_PUBLIC_KEY, contact.getKeyIdentifier());
            for (DropURL dropURL : contact.getDropUrls()) {
                jsonDropUrls.put(dropURL);
            }
            jsonObject.put(KEY_DROP_URLS, jsonDropUrls);
        } catch (JSONException e) {
            // Shouldn't be possible to trigger this exception
            throw new RuntimeException("Cannot build JSONObject", e);
        }
        return jsonObject;
    }

    /**
     * Exports the {@link Contact} information as a JSON string from an {@link Identity}
     *
     * @param identity {@link Identity} to export {@link Contact} information from
     * @return {@link Contact} information as JSON string
     */
    public static String exportIdentityAsContact(Identity identity) {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonDropUrls = new JSONArray();
        try {
            jsonObject.put(KEY_ALIAS, identity.getAlias());
            jsonObject.put(KEY_EMAIL, identity.getEmail());
            jsonObject.put(KEY_PHONE, identity.getPhone());
            jsonObject.put(KEY_PUBLIC_KEY, identity.getKeyIdentifier());
            for (DropURL dropURL : identity.getDropUrls()) {
                jsonDropUrls.put(dropURL);
            }
            jsonObject.put(KEY_DROP_URLS, jsonDropUrls);
        } catch (JSONException e) {
            // Shouldn't be possible to trigger this exception
            throw new RuntimeException("Cannot build JSONObject", e);
        }

        return jsonObject.toString();
    }

    /**
     * Parse a {@link Contact} from a {@link Contact} JSON string
     *
     * @param json {@link Contact} JSON string
     * @return {@link Contact} parsed from JSON string
     */
    public static Contact parseContactForIdentity(String json) throws JSONException, URISyntaxException, QblDropInvalidURL {

        return parseContactFromJSON(json);
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
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonContacts = jsonObject.getJSONArray(KEY_CONTACTS);
        for (int i = 0; i < jsonContacts.length(); i++) {
            JSONObject o = (JSONObject) jsonContacts.get(i);
            contacts.put(parseContactFromJSON(o.toString()));
        }
        return contacts;
    }

    private static Contact parseContactFromJSON(String json) throws JSONException, URISyntaxException, QblDropInvalidURL {

        JSONObject jsonObject = new JSONObject(json);

        Collection<DropURL> dropURLs = new ArrayList<>();
        String alias = jsonObject.getString(KEY_ALIAS);
        JSONArray jsonDropURLS = jsonObject.getJSONArray(KEY_DROP_URLS);
        for (int i = 0; i < jsonDropURLS.length(); i++) {
            dropURLs.add(new DropURL(jsonDropURLS.getString(i)));
        }
        String keyIdentifier = jsonObject.getString(KEY_PUBLIC_KEY);

        Contact contact = new Contact(alias, dropURLs, new QblECPublicKey(Hex.decode(keyIdentifier)));
        if (jsonObject.has(KEY_EMAIL)) {
            contact.setEmail(jsonObject.getString(KEY_EMAIL));
        }
        if (jsonObject.has(KEY_PHONE)) {
            contact.setPhone(jsonObject.getString(KEY_PHONE));
        }
        return contact;
    }
}
