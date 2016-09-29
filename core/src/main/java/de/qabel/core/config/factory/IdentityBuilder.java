package de.qabel.core.config.factory;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;

import java.util.LinkedList;
import java.util.List;

public class IdentityBuilder {
    private DropUrlGenerator dropUrlGenerator;

    private String alias = "new identity";
    private String email;
    private String phone;
    private String nickname;
    private QblECKeyPair keyPair;
    private List<DropURL> dropUrls = new LinkedList<>();

    public IdentityBuilder(DropUrlGenerator dropUrlGenerator) {
        this.dropUrlGenerator = dropUrlGenerator;
    }

    public IdentityBuilder withAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public IdentityBuilder dropAt(DropURL dropUrl) {
        dropUrls.add(dropUrl);
        return this;
    }

    public IdentityBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public IdentityBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public IdentityBuilder encryptWith(QblECKeyPair keyPair) {
        this.keyPair = keyPair;
        return this;
    }

    public Identity build() {
        if (dropUrls == null || dropUrls.isEmpty()) {
            dropAt(dropUrlGenerator.generateUrl());
        }
        if (keyPair == null) {
            keyPair = new QblECKeyPair();
        }

        Identity identity = new Identity(alias, dropUrls, keyPair);
        identity.setEmail(email);
        identity.setPhone(phone);
        return identity;
    }
}
