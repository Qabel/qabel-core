package de.qabel.core.storage;

public interface StorageResponseListener {
/**
 * <pre>
 *           0..1     0..1
 * StorageResponseListener ------------------------- StorageConnection
 *           storageResponseListener        &gt;       storageConnection
 * </pre>
 */
public void setStorageConnection(StorageConnection value);

public StorageConnection getStorageConnection();

public StorageBlob onStorageResponse(String/*No type specified*/ blob);

}
