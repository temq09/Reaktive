package com.badoo.reaktive.maybe

import com.badoo.reaktive.test.base.assertError
import com.badoo.reaktive.test.base.hasSubscribers
import com.badoo.reaktive.test.maybe.TestMaybe
import com.badoo.reaktive.test.maybe.TestMaybeObserver
import com.badoo.reaktive.test.maybe.assertComplete
import com.badoo.reaktive.test.maybe.assertNotComplete
import com.badoo.reaktive.test.maybe.assertNotSuccess
import com.badoo.reaktive.test.maybe.assertSuccess
import com.badoo.reaktive.test.maybe.test
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnErrorResumeNextTest :
    MaybeToMaybeTests by MaybeToMaybeTests<Unit>({ onErrorResumeNext { Unit.toMaybe() } }) {

    private val upstream = TestMaybe<Int?>()

    override fun produces_error_WHEN_upstream_produced_error() {
        // not applicable
    }

    @Test
    fun subscribes_to_resume_next_WHEN_upstream_produced_error() {
        val (errorResumeNext, _) = createTestWithMaybe()

        upstream.onError(Throwable())

        assertTrue(errorResumeNext.hasSubscribers)
    }

    @Test
    fun does_not_subscribe_to_resume_next_WHEN_upstream_produced_success() {
        val (errorResumeNext, _) = createTestWithMaybe()

        upstream.onSuccess(0)

        assertFalse(errorResumeNext.hasSubscribers)
    }

    @Test
    fun does_not_subscribe_to_resume_next_WHEN_upstream_completed() {
        val (errorResumeNext, _) = createTestWithMaybe()

        upstream.onComplete()

        assertFalse(errorResumeNext.hasSubscribers)
    }

    @Test
    fun disposes_upstream_WHEN_upstream_produced_error() {
        createTestWithMaybe()

        upstream.onError(Throwable())

        assertTrue(upstream.isDisposed)
    }

    @Test
    fun disposes_resume_next_WHEN_disposed() {
        val (errorResumeNext, observer) = createTestWithMaybe()

        upstream.onError(Throwable())
        observer.dispose()

        assertTrue(errorResumeNext.isDisposed)
    }

    @Test
    fun does_not_complete_WHEN_upstream_produced_error_and_resume_next_did_not_complete() {
        val (_, observer) = createTestWithMaybe()

        upstream.onError(Throwable())

        observer.assertNotComplete()
    }

    @Test
    fun completes_WHEN_upstream_produced_error_and_resume_next_completed() {
        val (errorResumeNext, observer) = createTestWithMaybe()

        upstream.onError(Throwable())
        errorResumeNext.onComplete()

        observer.assertComplete()
    }

    @Test
    fun does_not_produce_success_WHEN_upstream_produced_error_and_resume_next_did_not_produce_success() {
        val (_, observer) = createTestWithMaybe()

        upstream.onError(Throwable())

        observer.assertNotSuccess()
    }

    @Test
    fun produces_success_WHEN_upstream_produced_error_and_resume_next_produced_success() {
        val (errorResumeNext, observer) = createTestWithMaybe()

        upstream.onError(Throwable())
        errorResumeNext.onSuccess(0)

        observer.assertSuccess(0)
    }

    @Test
    fun produces_error_WHEN_upstream_produced_error_and_resume_next_produced_error() {
        val (errorResumeNext, observer) = createTestWithMaybe()

        upstream.onError(Throwable())
        val throwable = Throwable()
        errorResumeNext.onError(throwable)

        observer.assertError(throwable)
    }

    @Test
    fun produces_error_WHEN_upstream_produced_error_and_resume_next_supplier_produced_error() {
        val throwable = Throwable()
        val observer = upstream.onErrorResumeNext { throw throwable }.test()
        upstream.onError(Throwable())

        observer.assertError(throwable)
    }

    private fun createTestWithMaybe(): Pair<TestMaybe<Int?>, TestMaybeObserver<Int?>> {
        val errorResumeNext = TestMaybe<Int?>()
        val observer = upstream.onErrorResumeNext { errorResumeNext }.test()
        return errorResumeNext to observer
    }

}