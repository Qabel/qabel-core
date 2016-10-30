package de.qabel.core.event

import rx.Observable

interface EventSource {
    val events: Observable<Event>
    fun <T : Event> events(type: Class<T>): Observable<T> = events.ofType(type)
}
interface EventSink {
    fun push(event: Event)
}
interface EventDispatcher: EventSource, EventSink
inline fun <reified T : Event> EventSource.events(): Observable<T> = events.ofType(T::class.java)
