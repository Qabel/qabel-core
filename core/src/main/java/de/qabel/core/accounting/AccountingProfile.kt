package de.qabel.core.accounting

import de.qabel.core.config.SyncSettingItem

import java.util.ArrayList

class AccountingProfile : SyncSettingItem {
    var quota: Long = 0
    var prefixes: ArrayList<String>? = null

    constructor() {
        prefixes = ArrayList<String>()
    }

    constructor(quota: Long, prefix: String) {
        this.quota = quota
        prefixes = ArrayList<String>()
        prefixes!!.add(prefix)
    }

    fun addPrefix(prefix: String) {
        prefixes!!.add(prefix)
    }
}
