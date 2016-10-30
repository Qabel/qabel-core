package de.qabel.core.event

import rx.Observable

interface EventSource {
    fun events(): Observable<Event>
    fun <T : Event> events(type: Class<T>): Observable<T>
        = events().ofType(type).map { type.cast(it) }
}
interface EventSink {
    fun push(event: Event)
}
interface EventDispatcher: EventSource, EventSink
