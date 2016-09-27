package de.qabel.core.event

import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

class SubjectEventDispatcher : EventDispatcher {
    private val subject = SerializedSubject(PublishSubject.create<Event>())

    override fun events(): Observable<Event> {
        return subject
    }

    override fun push(event: Event) {
        subject.onNext(event)
    }
}
