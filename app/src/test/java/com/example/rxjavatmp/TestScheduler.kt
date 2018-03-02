package com.example.rxjavatmp

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.internal.operators.observable.ObservableSubscribeOn
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject
import org.junit.Test

/**
 * Created by yupenglei on 18/3/2.
 */
class TestScheduler {
    @Test
    fun test() {
        val observableOnSubscribe = ObservableOnSubscribe<Long> {
            it.onNext(1)
        }
        Observable
                .create(observableOnSubscribe)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())

        ObservableSubscribeOn.create(observableOnSubscribe)
//        Subject
        object :Observable<Long>(){
            override fun subscribeActual(observer: Observer<in Long>?) {

            }
        }
    }
    @Test fun test2(){
    }
}