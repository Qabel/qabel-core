package de.qabel.core.config;

import de.qabel.core.crypto.*;
import de.qabel.core.drop.DropURL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Identity extends Entity {
	private static final long serialVersionUID = 3949018763372790094L;

	private String alias;

	private String email;

	private String phone;

	private List<String> prefixes;

	@SerializedName("keys")
	private QblECKeyPair primaryKeyPair;

	public Identity(String alias, Collection<DropURL> drops,
					QblECKeyPair primaryKeyPair) {
		super(drops);
		this.setAlias(alias);
		this.setPrimaryKeyPair(primaryKeyPair);
		prefixes = new ArrayList<>();
	}

	/**
	 * Returns the list of prefixes of the identity
	 * @return prefixes
	 */
	public List<String> getPrefixes() {
		return prefixes;
	}

	/**
	 * Sets the list of prefixes of the identity.
	 * The list of prefixes is mutable, thus has no influence on the hashCode evaluation.
	 * @param prefixes the prefixes of the identity
	 */
	public void setPrefixes(List<String> prefixes) {
		this.prefixes = prefixes;
	}

	/**
	 * Returns the alias name of the identity.
	 * @return alias
	 */
	public String getAlias() {
		return this.alias;
	}

	/**
	 * Sets the alias address of the identity.
	 * The alias is mutable, thus has no influence on the hashCode evaluation.
	 * @param alias the alias of the identity
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Returns the email address of the identity.
	 * @return email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email address of the identity.
	 * The email address is optional, thus has no influence on the hashCode evaluation.
	 * @param email the email address of the identity
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns the phone number of the identity.
	 * @return phone the phone number of the identity
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the phone number of the identity.
	 * The phone number is optional, thus has no influence on the hashCode evaluation.
	 * @param phone the phone number of the identity
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Sets the primary key pair of the identity.
	 * @param key Primary Key pair of the identity.
	 */
	public void setPrimaryKeyPair(QblECKeyPair key)
	{
		this.primaryKeyPair = key;
	}

	/**
	 * Returns the primary key pair of the identity.
	 * @return QblECKeyPair
	 */
	public QblECKeyPair getPrimaryKeyPair()
	{
		return this.primaryKeyPair;
	}

	@Override
	public QblECPublicKey getEcPublicKey() {
		return this.getPrimaryKeyPair().getPub();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = super.hashCode();
		result = prime * result + ((primaryKeyPair == null) ? 0 : primaryKeyPair.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
		    return (false);
		}

		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Identity other = (Identity) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (primaryKeyPair == null) {
			if (other.primaryKeyPair != null)
				return false;
		} else if (!primaryKeyPair.equals(other.primaryKeyPair))
			return false;
		if (prefixes == null) {
			if (other.prefixes != null)
				return false;
		} else if (!prefixes.equals(other.prefixes))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;

		return true;
	}
}
