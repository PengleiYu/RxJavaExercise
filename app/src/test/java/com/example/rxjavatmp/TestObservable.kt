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
        val observable: Observable<Long> = SimpleObservable("A")
        observable.subscribe(object : SimpleObserver("B") {
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
        val disposableObservable: Observable<Long> = object : SimpleObservable("A"), Disposable {
            private var isDisposable: Boolean = false

            override fun subscribeActual(observer: Observer<in Long>) {
                observer.onSubscribe(this)
                for (i in 0..1000L) {
                    if (isDisposable) break
                    observer.onNext(i)
                    println("${getTag()} subscribeActual: $i")
                    try {
                        /**
                         * todo [Observer]调用[Disposable.dispose]函数会导致sleep被打断，原理未知
                         */
                        Thread.sleep(500)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        observer.onError(e)
                    }
                }
                observer.onComplete()
            }

            override fun isDisposed(): Boolean = isDisposed

            override fun dispose() {
                isDisposable = true
            }
        }
        disposableObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(object : SimpleObserver("B") {
                    override fun onNext(t: Long) {
                        super.onNext(t)
                        if (t == 10L) disposable?.dispose()
                    }
                })
        Thread.sleep(1_000_000)
    }
}