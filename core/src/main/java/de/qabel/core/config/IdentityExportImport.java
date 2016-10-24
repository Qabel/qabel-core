package de.qabel.core.config;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

public class IdentityExportImport {

    private static final String KEY_ALIAS = "alias";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PRIVATE_KEY = "private_key";
    private static final String KEY_PUBLIC_KEY = "public_key";
    private static final String KEY_PREFIXES = "prefixes";
    private static final String KEY_DROP_URLS = "drop_urls";

    /**
     * @param identity {@link Identity} to export
     * @return {@link Identity} information as JSON string
     */
    public static String exportIdentity(Identity identity) {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonDropUrls = new JSONArray();

        try {
            jsonObject.put(KEY_ALIAS, identity.getAlias());
            jsonObject.put(KEY_EMAIL, identity.getEmail());
            jsonObject.put(KEY_PHONE, identity.getPhone());
            jsonObject.put(KEY_PRIVATE_KEY, Hex.toHexString(identity.getPrimaryKeyPair().getPrivateKey()));
            JSONArray jsonPrefixes = new JSONArray();
            for (Prefix prefix : identity.getPrefixes()) {
                jsonPrefixes.put(prefix);
            }
            jsonObject.put(KEY_PREFIXES, jsonPrefixes);
            jsonObject.put(KEY_PUBLIC_KEY, Hex.toHexString(identity.getEcPublicKey().getKey()));

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
     * Parse a {@link Identity} from a {@link Identity} JSON string
     *
     * @param json {@link Identity} JSON string
     * @return {@link Identity} parsed from JSON string
     */
    public static Identity parseIdentity(String json) throws JSONException, URISyntaxException, QblDropInvalidURL {

        Collection<DropURL> dropURLs = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(json);
        String alias = jsonObject.getString(KEY_ALIAS);
        JSONArray jsonDropURLS = jsonObject.getJSONArray(KEY_DROP_URLS);
        for (int i = 0; i < jsonDropURLS.length(); i++) {
            dropURLs.add(new DropURL(jsonDropURLS.getString(i)));
        }

        QblECKeyPair qblECKeyPair = new QblECKeyPair(Hex.decode(jsonObject.getString(KEY_PRIVATE_KEY)));

        Identity identity = new Identity(alias, dropURLs, qblECKeyPair);

        if (jsonObject.has(KEY_PREFIXES)) {
            JSONArray jsonPrefixes = jsonObject.getJSONArray(KEY_PREFIXES);
            for (int i = 0; i < jsonPrefixes.length(); i++) {
                identity.getPrefixes().add(new Prefix(jsonPrefixes.getString(i)));
            }
        }
        if (jsonObject.has(KEY_EMAIL)) {
            identity.setEmail(jsonObject.getString(KEY_EMAIL));
        }
        if (jsonObject.has(KEY_PHONE)) {
            identity.setPhone(jsonObject.getString(KEY_PHONE));
        }
        return identity;
    }
}
