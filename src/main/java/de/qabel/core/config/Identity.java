package de.qabel.core.config;


public class Identity {
private int id;

public void setId(int value) {
   this.id = value;
}

public int getId() {
   return this.id;
}

private int updated;

public void setUpdated(int value) {
   this.updated = value;
}

public int getUpdated() {
   return this.updated;
}

private int created;

public void setCreated(int value) {
   this.created = value;
}

public int getCreated() {
   return this.created;
}

private int deleted;

public void setDeleted(int value) {
   this.deleted = value;
}

public int getDeleted() {
   return this.deleted;
}

private String alias;

public void setAlias(String value) {
   this.alias = value;
}

public String getAlias() {
   return this.alias;
}

private String privateKey;

public void setPrivateKey(String value) {
   this.privateKey = value;
}

public String getPrivateKey() {
   return this.privateKey;
}

private String inbox;

public void setInbox(String value) {
   this.inbox = value;
}

public String getInbox() {
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
