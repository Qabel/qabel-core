package de.qabel.core.config;

public interface EntityObservable {

	void addObserver(EntityObserver observer);
	void notifiedObserver();

}
