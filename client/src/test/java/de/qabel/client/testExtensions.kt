package de.qabel.client

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockito_kotlin.whenever
import rx.Observable
import java.util.concurrent.TimeUnit


infix fun <T:Any> T.isEqual(expected: T) = this shouldMatch equalTo(expected)

infix fun <T> T.eq(thing: T) {
    assertThat(this, equalTo(thing))
}


fun <T> Observable<T>.defaultTimeout() = timeout(100, TimeUnit.MILLISECONDS)

infix fun <T> Observable<T>.evalsTo(thing: T) {
    assertThat(this.defaultTimeout().toBlocking().first(), equalTo(thing))
}

infix fun <T> Observable<T>.matches(matcher: Matcher<T>) {
    assertThat(this.defaultTimeout().toBlocking().first(), matcher)
}

infix fun <T> Observable<T>.errorsWith(error: Throwable) {
    var e : Throwable? = null
    this.defaultTimeout().toBlocking().subscribe({}, { e = it})
    try { e eq error } catch (ex : AssertionError) { e?.printStackTrace(); throw ex }
}

fun <T> stubMethod(methodCall: T, result: T)
        = whenever(methodCall).thenReturn(result)
