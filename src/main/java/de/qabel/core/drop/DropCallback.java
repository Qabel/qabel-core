package de.qabel.core.drop;

interface DropCallback<T extends ModelObject> {
	void onDropMessage(DropMessage<T> message);
}