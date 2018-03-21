package com.example.rxjavatmp

import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * 尝试实现多观察者的数据源
 * Created by yupenglei on 18/3/7.
 */
class TestMultiObserverObservable {
    @Test
    fun testObservableA() {
        val ob1 = SimpleObserver<Long>("A")
        val ob2 = SimpleObserver<Long>("B")
        val source = Observable.interval(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .let { ObservableA(it) }
        source
                //todo 导致了空指针异常
//                .observeOn(Schedulers.newThread())
                .subscribe(ob1)
        Thread.sleep(3000)
        source
//                .observeOn(Schedulers.newThread())
                .subscribe(ob2)
        Thread.sleep(1_000_000)
    }

    /**
     * 该实现很拙劣，且[Observable.observeOn]会导致空指针
     */
    class ObservableA<T>(source: Observable<T>) : ObservableSource<T> {
        private val list = mutableListOf<Observer<in T>>()

        init {
            source.subscribe { t ->
                println("list: $list")
                list.forEach {
                    println("it: $it")
                    it.onNext(t)
                }
            }
        }

        override fun subscribe(observer: Observer<in T>) {
            println("subscribeActual: $observer")
            list.add(observer)
        }
    }

    @Test
    fun testObservableB() {
        val observable = Observable.create(ObservableB())
                .subscribeOn(Schedulers.single())
//                .subscribeOn(Schedulers.io())
        observable
//                .observeOn(Schedulers.computation())
                .subscribe(SimpleObserver<Long>("A"))
        Thread.sleep(2000)
        observable
                .observeOn(Schedulers.newThread())
                .subscribe(SimpleObserver<Long>("B"))
        Thread.sleep(1_000_000)
    }

    /**
     * 该实现下[Observable.subscribeOn]将无效
     * 不过也符合情理，毕竟发射源是独立的，不能被观察者指定线程
     */
    class ObservableB : ObservableOnSubscribe<Long> {
        private val list = mutableListOf<ObservableEmitter<Long>>()

        init {
            source2()
        }

        private fun source2() {
            Observable.interval(500L, TimeUnit.MILLISECONDS)
                    .subscribe({ n -> list.forEach { it.onNext(n) } },
                            { t -> list.forEach { it.onError(t) } },
                            { list.forEach { it.onComplete() } })
        }

        private fun source1() {
            Thread {
                var n = 0L
                while (true) {
                    ThreadLogger.log("send $n")
                    list.forEach { it.onNext(n) }
                    n++
                    Thread.sleep(500)
                }
            }.start()
        }

        override fun subscribe(emitter: ObservableEmitter<Long>) {
            list.add(emitter)
        }
    }
}