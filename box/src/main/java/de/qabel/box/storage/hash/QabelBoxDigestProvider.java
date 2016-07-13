package de.qabel.box.storage.hash;

import java.security.Provider;

public class QabelBoxDigestProvider extends Provider {
    public QabelBoxDigestProvider() {
        super("Qabel", 0.1, "Qabel Security Provider");
        put("MessageDigest.Blake2b", Blake2b.class.getName() + "$Digest");
    }
}
