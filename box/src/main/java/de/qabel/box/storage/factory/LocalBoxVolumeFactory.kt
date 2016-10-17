package de.qabel.box.storage.factory

import de.qabel.box.storage.BoxVolumeImpl
import de.qabel.box.storage.LocalReadBackend
import de.qabel.box.storage.LocalWriteBackend
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import java.nio.file.Path
import java.util.*

class LocalBoxVolumeFactory(private val tmpDir: Path, private val deviceId: String) : BoxVolumeFactory {
    private var prefix: String? = null

    constructor(tmpDir: Path, deviceId: String, prefix: String) : this(tmpDir, deviceId) {
        this.prefix = prefix
    }

    override fun getVolume(account: Account, identity: Identity, type: Prefix.TYPE): BoxVolumeImpl {
        if (prefix == null) {
            if (identity.prefixes.isEmpty()) {
                identity.prefixes.add(Prefix(UUID.randomUUID().toString()))
            }
            prefix = identity.prefixes[0].prefix
        }
        return BoxVolumeImpl(
                LocalReadBackend(tmpDir),
                LocalWriteBackend(tmpDir),
                identity.primaryKeyPair,
                deviceId.toByteArray(),
                tmpDir.toFile(),
                prefix!!)
    }
}
