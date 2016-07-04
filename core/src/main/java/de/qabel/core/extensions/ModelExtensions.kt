package de.qabel.core.extensions

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity

fun Identity.toContact(): Contact {
    val contact = Contact(alias, dropUrls, ecPublicKey);
    contact.email = email;
    contact.phone = phone;
    return contact;
}
