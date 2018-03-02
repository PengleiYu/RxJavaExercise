package com.example.rxjavatmp

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.junit.Test

/**
 * Created by yupenglei on 18/3/1.
 */
class TestObservable {
    @Test
    fun testObservableCreate() {
        val observable = Observable.create(ObservableOnSubscribe<Long> { emitter ->
            (0L..100).forEach { emitter.onNext(it) }
        })
        observable.subscribe(object : SimpleObserver("A") {})
    }

    /**
     * 测试自制的Observable
     */
    @Test
    fun testSimpleObservable() {
        val observable: Observable<Long> = object : Observable<Long>() {
            /**
             * [Observable.subscribeActual]是真正调用观察者进行发布的地方
             * 由于未实现[Disposable]，所以不能调用observer的onSubscribe函数
             */
            override fun subscribeActual(observer: Observer<in Long>) {
//                observer.onSubscribe()
                (0L..10).forEach { observer.onNext(it) }
                observer.onComplete()
            }
        }
        observable.subscribe(object : SimpleObserver("A") {
            override fun onNext(t: Long) {
                super.onNext(t)
                if (t == 5L) disposable?.dispose()
            }
        })
    }

    /**
     * 测试自制的DisposableObservable
     * 可销毁的Observable
     */
    @Test
    fun testDisposableObservable() {
        val disposableObservable: Observable<Long> = object : Observable<Long>(), Disposable {
            private var isDisposed = false
            override fun isDisposed(): Boolean = isDisposed

            override fun dispose() {
                isDisposed = true
            }

            override fun subscribeActual(observer: Observer<in Long>) {
                observer.onSubscribe(this)
                for (i in 0L..100) {
                    if (isDisposed) break
                    println("${Thread.currentThread().name} subscribeActual: $i")
                    observer.onNext(i)
                    try {
                        Thread.sleep(500)
                    } catch (e: Exception) {
                        observer.onError(e)
                    }
                }
                observer.onComplete()
            }
        }
        disposableObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(object : SimpleObserver("A") {
                    override fun onNext(t: Long) {
                        super.onNext(t)
                        if (t == 10L) disposable?.dispose()
                    }
                })
        Thread.sleep(1_000_000)
    }
}