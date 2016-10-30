package de.qabel.core.event

import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

class SubjectEventDispatcher : EventDispatcher {
    override val events = SerializedSubject(PublishSubject.create<Event>())

    override fun push(event: Event) {
        events.onNext(event)
    }
}
