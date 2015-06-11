package de.qabel.core.config;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.meanbean.lang.Factory;

import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

/**
 * DropUrlListTestFactory
 * Creates distinct Collections of class DropURL
 * Attention: For testing purposes only
 */
class DropUrlListTestFactory implements Factory<Collection<DropURL>> {
	int i = 100;

	@Override
	public Collection<DropURL> create() {
		Collection<DropURL> dropList = new ArrayList<DropURL>();
		DropURL dropURL1 = null;
		DropURL dropURL2 = null;
		if (i > 997) {
			i = 100;
		}
		String strUrl1 = "http://drop.test.de/0123456789012345678901234567890123456789" + i++;
		String strUrl2 = "http://drop.test.de/0123456789012345678901234567890123456789" + i++;

		try {
			dropURL1 = new DropURL(strUrl1);
			dropURL2 = new DropURL(strUrl2);
		} catch (QblDropInvalidURL | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dropList.add(dropURL1);
		dropList.add(dropURL2);
		return dropList;
	}
}
