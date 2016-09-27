package de.qabel.core.event

import rx.Observable

interface EventSource {
    fun events(): Observable<Event>
}
interface EventSink {
    fun push(event: Event)
}
interface EventDispatcher: EventSource, EventSink
