package de.qabel.box.storage.hash;

import org.spongycastle.crypto.digests.Blake2bDigest;
import org.spongycastle.jcajce.provider.digest.BCMessageDigest;

public class Blake2b {
    private Blake2b() {}

    static public class Digest
        extends BCMessageDigest
    {
        public Digest()
        {
            super(new Blake2bDigest());
        }
    }
}
