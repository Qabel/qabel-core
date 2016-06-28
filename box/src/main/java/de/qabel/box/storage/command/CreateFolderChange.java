package de.qabel.box.storage.command;

import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.DirectoryMetadata;
import de.qabel.box.storage.JdbcDirectoryMetadata;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.box.storage.exceptions.QblStorageException;
import org.spongycastle.crypto.params.KeyParameter;

public class CreateFolderChange implements DirectoryMetadataChange<ChangeResult<BoxFolder>> {
    private CryptoUtils cryptoUtils = new CryptoUtils();
    private String name;
    private byte[] deviceId;
    private final KeyParameter secretKey;

    public CreateFolderChange(String name, byte[] deviceId) {
        this.name = name;
        this.deviceId = deviceId;
        secretKey = cryptoUtils.generateSymmetricKey();
    }

    @Override
    public ChangeResult<BoxFolder> execute(DirectoryMetadata parentDM) throws QblStorageException {
        if (!(parentDM instanceof JdbcDirectoryMetadata)) {
            throw new IllegalStateException("cannot create folder without a JdbcDirectoryMetadata");
        }
        for (BoxFolder folder : parentDM.listFolders()) {
            if (folder.getName().equals(name)) {
                ChangeResult<BoxFolder> result = new ChangeResult<>(folder);
                result.setSkipped(true);
                return result;
            }
        }

        JdbcDirectoryMetadata parent = (JdbcDirectoryMetadata)parentDM;
        JdbcDirectoryMetadata dm = JdbcDirectoryMetadata.newDatabase(null, deviceId, parent.getTempDir());
        BoxFolder folder = new BoxFolder(dm.getFileName(), name, secretKey.getKey());
        parentDM.insertFolder(folder);
        dm.commit();
        return new ChangeResult<>(dm, folder);
    }
}
