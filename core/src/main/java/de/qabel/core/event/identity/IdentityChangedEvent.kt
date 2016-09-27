package de.qabel.core.event.identity

import de.qabel.core.config.Identity
import de.qabel.core.event.Event

interface IdentityChangedEvent : Event {
    val identity: Identity
}
