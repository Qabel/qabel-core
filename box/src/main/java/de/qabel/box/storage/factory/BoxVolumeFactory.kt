package de.qabel.box.storage.factory

import de.qabel.box.storage.BoxVolume
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.config.Prefix.TYPE.USER

interface BoxVolumeFactory {
    fun getVolume(account: Account, identity: Identity) = getVolume(account, identity, USER)
    fun getVolume(account: Account, identity: Identity, type : Prefix.TYPE): BoxVolume
}
