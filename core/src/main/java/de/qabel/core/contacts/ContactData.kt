package de.qabel.core.contacts

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity

data class ContactData(val contact : Contact, val identities : List<Identity>, val isIdentity : Boolean)
