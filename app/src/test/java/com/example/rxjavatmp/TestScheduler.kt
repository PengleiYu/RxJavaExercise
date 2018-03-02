package com.example.rxjavatmp

import io.reactivex.schedulers.Schedulers
import org.junit.Test

/**
 * Created by yupenglei on 18/3/2.
 */
class TestScheduler {
    @Test
    fun test() {
        val observable1 = SimpleObservable2("A")
//        observable1
//                .subscribeOn(Schedulers.computation())
//                .observeOn(Schedulers.io())
//                .subscribe(SimpleObserver("B"))
//        observable1.subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.computation())
//                .subscribe(SimpleObserver("C"))

        val observable2 = observable1
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
        observable2.subscribe(SimpleObserver("B"))
        observable1.start()
        Thread.sleep(1000)
        observable2.subscribe(SimpleObserver("C"))


//        val observable2 = SimpleObservable("C")
        //todo 似乎必须subscribeOn
//        observable2.observeOn(Schedulers.io())
//                .subscribe(SimpleObserver("D"))
        Thread.sleep(1000_000)
    }
}