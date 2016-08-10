package de.qabel.box.storage.hash

import org.spongycastle.crypto.digests.Blake2bDigest
import org.spongycastle.jcajce.provider.digest.BCMessageDigest

object Blake2b {

    class Digest : BCMessageDigest(Blake2bDigest())
}
