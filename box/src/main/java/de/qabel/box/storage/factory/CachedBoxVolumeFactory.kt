package de.qabel.box.storage.factory

import de.qabel.box.storage.BoxVolume
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix

class CachedBoxVolumeFactory(private val factory: BoxVolumeFactory) : BoxVolumeFactory {
    private val volumes = mutableMapOf<Account, MutableMap<Identity, MutableMap<Prefix.TYPE, BoxVolume>>>()

    @Synchronized override fun getVolume(account: Account, identity: Identity, type: Prefix.TYPE): BoxVolume {
        return volumes
            .getOrPut(account) { mutableMapOf<Identity, MutableMap<Prefix.TYPE, BoxVolume>>() }
            .getOrPut(identity) { mutableMapOf<Prefix.TYPE, BoxVolume>() }
            .getOrPut(type) { factory.getVolume(account, identity) }
    }
}
