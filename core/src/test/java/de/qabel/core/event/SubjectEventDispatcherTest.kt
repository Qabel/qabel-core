package de.qabel.core.event

import de.qabel.core.extensions.letApply
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import rx.observers.TestSubscriber

class SubjectEventDispatcherTest {
    @Test
    fun providesFilteredEventStream() {
        val dispatcher = SubjectEventDispatcher()
        val testsubscriber = TestSubscriber<Event>()

        dispatcher.events(TestEventA::class.java).subscribe(testsubscriber)
        dispatcher.push(TestEventB())
        val eventA: TestEventA = TestEventA().letApply { dispatcher.push(it) }

        val onNextEvents = testsubscriber.onNextEvents
        assertEquals(1, onNextEvents.size)
        assertSame(eventA, onNextEvents.first())

    }
}
class TestEventA : Event
class TestEventB : Event
