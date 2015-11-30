package de.qabel.core.accounting;

import de.qabel.core.config.SyncSettingItem;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class AccountingProfile extends SyncSettingItem{
	
	private Integer quota;
	private String prefix;
	private ArrayList<String> prefixes;

	public AccountingProfile() {
		this.prefixes = new ArrayList<>();
	}

	public AccountingProfile(int quota, String prefix) {
		this.quota = quota;
		this.prefix = prefix;
		this.prefixes = new ArrayList<>();
		this.prefixes.add(prefix);
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

}
