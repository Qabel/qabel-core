package de.qabel.core.drop;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AcknowledgeIdGenerationTest {
    private Identity sender;

    @Before
    public void setup() {
        sender = new Identity("Bernd", null, new QblECKeyPair());
    }

    @Test
    public void testDefaultDisabledAck() {
        DropMessage dm = new DropMessage(sender, "", "");
        assertEquals(DropMessage.NOACK, dm.getAcknowledgeID());
    }

    @Test
    public void testSwitchAck() {
        DropMessage dm = new DropMessage(sender, "", "");
        dm.enableAcknowledging(true);
        assertNotEquals(DropMessage.NOACK, dm.getAcknowledgeID());
        dm.enableAcknowledging(false);
        assertEquals(DropMessage.NOACK, dm.getAcknowledgeID());
    }
}
