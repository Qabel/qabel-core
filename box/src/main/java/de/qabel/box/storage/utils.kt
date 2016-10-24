package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageIOFailure
import de.qabel.box.storage.exceptions.QblStorageInvalidKey
import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.exceptions.QblInvalidCredentials
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.PersistenceException
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.security.InvalidKeyException

@Throws(IOException::class, QblInvalidCredentials::class, PersistenceException::class)
fun createNewPrefix(identity: Identity, boxClient: BoxClient, identityRepository: IdentityRepository): Prefix {
    boxClient.createPrefix()
    val prefixes = boxClient.prefixes
    val prefix = prefixes[prefixes.size - 1]
    val prefixInstance = Prefix(prefix)
    identity.prefixes.add(prefixInstance)
    identityRepository.save(identity)
    return prefixInstance
}

@Throws(IOException::class, QblInvalidCredentials::class)
fun createIndex(
    identity: Identity,
    prefix: Prefix,
    directoryFactory: DirectoryMetadataFactory,
    writeBackend: StorageWriteBackend
) = createIndex(
    directoryFactory,
    writeBackend,
    CryptoUtils(),
    RootRefCalculator().rootFor(identity.primaryKeyPair.privateKey, prefix.type, prefix.prefix),
    identity.primaryKeyPair
)

@Throws(IOException::class, QblInvalidCredentials::class)
fun createIndex(
    directoryFactory: DirectoryMetadataFactory,
    writeBackend: StorageWriteBackend,
    cryptoUtils: CryptoUtils,
    rootRef: String,
    keyPair: QblECKeyPair
): Unit {
    val dm = directoryFactory.create("")
    try {
        val plaintext = IOUtils.toByteArray(FileInputStream(dm.path))
        val encrypted = cryptoUtils.createBox(keyPair, keyPair.pub, plaintext, 0)
        writeBackend.upload(rootRef, ByteArrayInputStream(encrypted))
    } catch (e: IOException) {
        throw QblStorageIOFailure(e)
    } catch (e: InvalidKeyException) {
        throw QblStorageInvalidKey(e)
    }
}
