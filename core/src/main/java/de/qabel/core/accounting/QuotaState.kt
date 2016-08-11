package de.qabel.core.accounting

import org.apache.commons.io.FileUtils

class QuotaState(val quota: Long, val size: Long) {


    /**
     * debug only feature

     * @return
     */
    override fun toString(): String {
        return FileUtils.byteCountToDisplaySize(quota - size) + " free / " + FileUtils.byteCountToDisplaySize(quota)
    }
}
