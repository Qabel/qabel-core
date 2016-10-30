package de.qabel.core.event

import de.qabel.core.extensions.letApply
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class SubjectEventDispatcherTest {
    val dispatcher = SubjectEventDispatcher()
    val testsubscriber = TestSubscriber<Event>()

    @Test
    fun providesFilteredEventStream() {
        dispatcher.events(TestEventA::class.java).subscribe(testsubscriber)
        dispatcher.push(TestEventB())
        val eventA: TestEventA = TestEventA().letApply { dispatcher.push(it) }

        val onNextEvents = testsubscriber.onNextEvents
        assertEquals(1, onNextEvents.size)
        assertSame(eventA, onNextEvents.first())
    }

    @Test
    fun providesFilteredEventStreamByTypeInference() {
        val events: Observable<TestEventA> = dispatcher.events()
        events.subscribe(testsubscriber)
        dispatcher.push(TestEventB())
        val eventA: TestEventA = TestEventA().letApply { dispatcher.push(it) }

        val onNextEvents = testsubscriber.onNextEvents
        assertEquals(1, onNextEvents.size)
        assertSame(eventA, onNextEvents.first())
    }
}
class TestEventA : Event
class TestEventB : Event
