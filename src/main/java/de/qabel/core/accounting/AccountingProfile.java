package de.qabel.core.accounting;

import de.qabel.core.config.SyncSettingItem;

public class AccountingProfile extends SyncSettingItem{

	public AccountingProfile() {
	}

	public AccountingProfile(int quota, String prefix) {

		this.quota = quota;
		this.prefix = prefix;
	}

	public Integer getQuota() {

		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	private Integer quota;
	private String prefix;
}
