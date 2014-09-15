package de.qabel.core.drop;

import java.util.Set;
import java.util.HashSet;

public class DropController {
public DropListener register(String/*No type specified*/ drop) {
   // TODO implement this operation
   throw new UnsupportedOperationException("not implemented");
}

	/**
	 * <pre>
	 *           0..*     0..*
	 * DropController ------------------------> DropListener
	 *           dropController        &gt;       dropListener
	 * </pre>
	 */
	private Set<DropListener> dropListener;

	public Set<DropListener> getDropListener() {
		if (this.dropListener == null) {
			this.dropListener = new HashSet<DropListener>();
		}
		return this.dropListener;
	}

}
