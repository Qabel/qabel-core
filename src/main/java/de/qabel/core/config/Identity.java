package de.qabel.core.config;

import de.qabel.core.crypto.*;
import java.net.URL;

public class Identity {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	private String alias;
	private QblKeyPair encryptionKey;
	private QblKeyPair signatureKey;
	private URL inbox;

	public void setId(int value) {
		this.id = value;
	}

	public int getId() {
		return this.id;
	}

	public void setUpdated(int value) {
		this.updated = value;
	}

	public int getUpdated() {
		return this.updated;
	}

	public void setCreated(int value) {
		this.created = value;
	}

	public int getCreated() {
		return this.created;
	}

	public void setDeleted(int value) {
		this.deleted = value;
	}

	public int getDeleted() {
		return this.deleted;
	}

	public void setAlias(String value) {
		this.alias = value;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setEncryptionKey(QblKeyPair value) {
		this.encryptionKey = value;
	}

	public QblKeyPair getEncryptionKey() {
		return this.encryptionKey;
	}

	public void setSignatureKey(QblKeyPair value) {
		this.signatureKey = value;
	}

	public QblKeyPair getSignatureKey() {
		return this.signatureKey;
	}

	public void setInbox(URL inbox) {
		this.inbox = inbox;
	}

	public URL getInbox() {
		return this.inbox;
	}

	/**
	 * <pre>
	 *           0..*     0..1
	 * Identity ------------------------- Identities
	 *           identity        &lt;       identities
	 * </pre>
	 */
	private Identities identities;

	public void setIdentities(Identities value) {
		this.identities = value;
	}

	public Identities getIdentities() {
		return this.identities;
	}

}
