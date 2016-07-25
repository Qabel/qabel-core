package de.qabel.core.accounting;

import org.apache.commons.io.FileUtils;

public class QuotaState {
    private long quota;
    private long size;

    public QuotaState(long quota, long size) {
        this.quota = quota;
        this.size = size;
    }

    public long getQuota() {
        return quota;
    }

    public long getSize() {
        return size;
    }


    /**
     * debug only feature
     *
     * @return
     */
    @Override
    public String toString() {
        return FileUtils.byteCountToDisplaySize(quota - size) + " free / " + FileUtils.byteCountToDisplaySize(quota);
    }
}
