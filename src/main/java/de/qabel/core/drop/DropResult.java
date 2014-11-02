/**
 * 
 */
package de.qabel.core.drop;

import java.util.Collections;
import java.util.List;

/**
 * Class DropResult: Save the return value of every contact and the overview
 * whether everything is okay
 *
 */
public class DropResult {

	private boolean ok = false;
	private List<DropResultPair> pairs;

	/**
	 * Constructor
	 * @param ok State of the shipping
	 * @param pairs List of all results
	 */
	public DropResult(boolean ok, List<DropResultPair> pairs) {
		this.ok = ok;
		this.pairs = pairs;
	}

	/**
	 * Return the overview state of the complete shipping 
	 * @return State of the shipping
	 */
	public boolean isOk() {
		return this.ok;
	}

	/**
	 * Return the list of all results of the contacts
	 * @return List of all results
	 */
	public List<DropResultPair> getPairs() {
		return Collections.unmodifiableList(this.pairs);
	}

	/**
	 * Add one result pair
	 * @param pair Object of DropResultPair
	 */
	public void addPairs(DropResultPair pair) {
		this.pairs.add(pair);
	}
}
