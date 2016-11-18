package de.qabel.box.storage.hash;

import java.security.Provider;
import java.util.UUID;

public class QabelBoxDigestProvider extends Provider {
    public static boolean randomizeName;

    public QabelBoxDigestProvider() {
        super("Qabel" + (randomizeName ? UUID.randomUUID() : ""), 0.1, "Qabel Security Provider");
        put("MessageDigest.Blake2b", Blake2b.class.getName() + "$Digest");
    }
}
