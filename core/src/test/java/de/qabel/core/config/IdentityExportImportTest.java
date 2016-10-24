package de.qabel.core.config;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class IdentityExportImportTest {

    @Test
    public void testExportImportIdentity() throws Exception {

        QblECKeyPair qblECKeyPair = new QblECKeyPair();
        Collection<DropURL> dropURLs = new ArrayList<>();
        dropURLs.add(new DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"));
        dropURLs.add(new DropURL("http://localhost:6000/0000000000000000000000000000000000000000000"));

        Identity identity = new Identity("Identity", dropURLs, qblECKeyPair);
        identity.setId(1);
        identity.setPhone("049111111");
        identity.setEmail("test@example.com");
        identity.getPrefixes().add(new Prefix("prefix1"));
        identity.getPrefixes().add(new Prefix("prefix2"));

        String exportedIdentity = IdentityExportImport.exportIdentity(identity);

        Identity importedIdentity = IdentityExportImport.parseIdentity(exportedIdentity);

        assertEquals(0, importedIdentity.getId());
        assertEquals(identity.getEmail(), importedIdentity.getEmail());
        assertEquals(identity.getPhone(), importedIdentity.getPhone());
        assertEquals(identity.getPrimaryKeyPair(), importedIdentity.getPrimaryKeyPair());
        assertEquals(identity.getDropUrls(), importedIdentity.getDropUrls());
        assertEquals(identity.getPrefixes(), importedIdentity.getPrefixes());
    }

    @Test
    public void testExportImportIdentityMissingOptionals() throws Exception {

        QblECKeyPair qblECKeyPair = new QblECKeyPair();
        Collection<DropURL> dropURLs = new ArrayList<>();
        dropURLs.add(new DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"));
        dropURLs.add(new DropURL("http://localhost:6000/0000000000000000000000000000000000000000000"));

        Identity identity = new Identity("Identity", dropURLs, qblECKeyPair);

        String exportedIdentity = IdentityExportImport.exportIdentity(identity);

        Identity importedIdentity = IdentityExportImport.parseIdentity(exportedIdentity);

        assertEquals(identity.getId(), importedIdentity.getId());
        assertEquals(identity.getEmail(), importedIdentity.getEmail());
        assertEquals(identity.getPhone(), importedIdentity.getPhone());
        assertEquals(identity.getPrimaryKeyPair(), importedIdentity.getPrimaryKeyPair());
        assertEquals(identity.getDropUrls(), importedIdentity.getDropUrls());
        assertEquals(identity.getPrefixes(), importedIdentity.getPrefixes());
    }
}
