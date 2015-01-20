package de.qabel.core.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.meanbean.lang.EquivalentFactory;

import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import de.qabel.core.drop.DropURL;

/**
 * IdentityEquivalentTestFactory
 * Creates logically equivalent instances of class Identity
 * Attention: For testing purposes only
 */
class IdentityEquivalentTestFactory implements EquivalentFactory<Identity> {
	QblPrimaryKeyPair qpkp;
	List<DropURL> dropList;
	long created = new Date().getTime();

	IdentityEquivalentTestFactory() {
		QblKeyFactory kf = QblKeyFactory.getInstance();
		qpkp = kf.generateQblPrimaryKeyPair();

		DropUrlListTestFactory dropListFactory = new DropUrlListTestFactory();
		dropList = new ArrayList<DropURL>(dropListFactory.create());
	}

	@Override
	public Identity create() {
		Identity identity = new Identity("alias", dropList, qpkp);
		identity.setCreated(created);
		return identity;
	}
}
