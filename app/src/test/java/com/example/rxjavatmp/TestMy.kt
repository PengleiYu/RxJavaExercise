package com.example.rxjavatmp

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by yupenglei on 18/3/7.
 */
class TestMy {
    @Test
    fun test() {
        val ob1 = com.example.rxjavatmp.SimpleObserver<Long>("A")
        val ob2 = com.example.rxjavatmp.SimpleObserver<Long>("B")
        val source = Observable.interval(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .let { ObservableA(it) }
        source
                //todo 导致了空指针异常
                .observeOn(Schedulers.newThread())
                .subscribe(ob1)
        Thread.sleep(3000)
        source.observeOn(Schedulers.newThread()).subscribe(ob2)
        Thread.sleep(1_000_000)
    }

    class ObservableA<T>(source: Observable<T>) : Observable<T>() {
        private val list = mutableListOf<Observer<in T>>()

        init {
            source.subscribe { t ->
                println(list)
                list.forEach {
                    println(it)
                    it.onNext(t)
                }
            }
        }

        override fun subscribeActual(observer: Observer<in T>) {
            list.add(observer)
        }
    }
}