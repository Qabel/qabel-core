package de.qabel.box;

import de.qabel.core.crypto.CryptoUtils;
import org.junit.Test;
import org.spongycastle.crypto.params.KeyParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlaceholderTest {
    @Test
    public void compileCheckTest() {
        assertTrue(new Placeholder() instanceof Placeholder);
    }

    @Test
    public void dependencyTest() throws Exception {
        CryptoUtils utils = new CryptoUtils();
        String message = "testmessage";
        KeyParameter key = utils.generateSymmetricKey();
        byte[] encryptedMessage = utils.encrypt(key, "?".getBytes(), message.getBytes(), "?".getBytes());
        String decryptedMessage = new String(utils.decrypt(key, "?".getBytes(), encryptedMessage, "?".getBytes()));
        assertEquals(decryptedMessage, message);
    }
}
