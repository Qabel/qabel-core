package de.qabel.core.crypto;

public class DecryptedPlaintext {

    private QblECPublicKey senderKey;
    private byte[] plaintext;

    public DecryptedPlaintext(QblECPublicKey senderKey, byte[] plaintext) {
        this.senderKey = senderKey;
        this.plaintext = plaintext;
    }

    public QblECPublicKey getSenderKey() {
        return senderKey;
    }

    public byte[] getPlaintext() {
        return plaintext;
    }
}
