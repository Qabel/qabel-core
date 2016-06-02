package de.qabel.core.accounting;

import de.qabel.core.config.SyncSettingItem;

import java.util.ArrayList;

public class AccountingProfile extends SyncSettingItem {
    private Integer quota;
    private ArrayList<String> prefixes;

    public AccountingProfile() {
        prefixes = new ArrayList<>();
    }

    public AccountingProfile(int quota, String prefix) {
        this.quota = quota;
        prefixes = new ArrayList<>();
        prefixes.add(prefix);
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public ArrayList<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(ArrayList<String> prefixes) {
        this.prefixes = prefixes;
    }

    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }
}
