package de.qabel.core.config

import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL
import org.meanbean.lang.Factory

import java.net.URISyntaxException
import java.util.ArrayList

/**
 * DropUrlListTestFactory
 * Creates distinct Collections of class DropURL
 * Attention: For testing purposes only
 */
internal class DropUrlListTestFactory : Factory<Collection<DropURL>> {
    var i = 100

    override fun create(): Collection<DropURL> {
        val dropList = ArrayList<DropURL>()
        if (i > 997) {
            i = 100
        }
        val strUrl1 = "http://drop.test.de/0123456789012345678901234567890123456789" + i++
        val strUrl2 = "http://drop.test.de/0123456789012345678901234567890123456789" + i++

        val dropURL1 = DropURL(strUrl1)
        val dropURL2 = DropURL(strUrl2)
        dropList.add(dropURL1)
        dropList.add(dropURL2)
        return dropList
    }
}
