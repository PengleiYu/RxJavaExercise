package com.example.rxjavatmp

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import org.junit.Test

/**
 * Created by yupenglei on 18/3/1.
 */
class TestObservable {
    @Test
    fun testObservable() {
        val observable = Observable.create(ObservableOnSubscribe<Long> { emitter ->
            (0L..100).forEach { emitter.onNext(it) }
        })

    }
}