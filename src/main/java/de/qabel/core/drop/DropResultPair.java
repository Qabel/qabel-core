/**
 * 
 */
package de.qabel.core.drop;

import de.qabel.core.config.Contact;

/**
 * Class DropResultPair: Save the return value of a contact
 *
 */
public class DropResultPair {
	private Contact contact;
	private boolean ok;

	/**
	 * Constructor
	 * @param contact Contact object
	 * @param ok Return state
	 */
	DropResultPair(Contact contact, boolean ok) {
		this.contact = contact;
		this.ok = ok;
	}

	/**
	 * Get contact
	 * @return Contact object
	 */
	public Contact getContact() {
		return this.contact;
	}

	/**
	 * Get the return state of the shipping
	 * @return Return state
	 */
	public boolean isOk() {
		return this.ok;
	}

}
