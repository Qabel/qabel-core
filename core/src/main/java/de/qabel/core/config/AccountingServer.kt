package de.qabel.core.config

import java.net.URI

class AccountingServer(var uri: URI?, val blockUri: URI, var username: String?, var password: String?) : SyncSettingItem() {

    var authToken: String? = null
}
