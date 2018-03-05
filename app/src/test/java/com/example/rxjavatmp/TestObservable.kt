package com.example.rxjavatmp

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by yupenglei on 18/3/1.
 */
class TestObservable {
    /**
     * 测试各种[Observable]
     */
    @Test
    fun testObservable() {
        //========================================================================================
        // 顶级接口
        ObservableSource<Long> {
        }
        SingleSource<Long> {
        }
        CompletableSource {
        }
        MaybeSource<Long> {
        }
        //========================================================================================
        // 二级抽象类
        object : Observable<Long>() {
            override fun subscribeActual(observer: Observer<in Long>?) {}
        }
        object : Single<Long>() {
            override fun subscribeActual(observer: SingleObserver<in Long>) {}
        }
        object : Completable() {
            override fun subscribeActual(s: CompletableObserver?) {}
        }
        object : Maybe<Long>() {
            override fun subscribeActual(observer: MaybeObserver<in Long>?) {}
        }
        //========================================================================================
        // 工具方法
        val successCallback = { s: String -> println(s) }
        val errorCallback = { t: Throwable -> t.printStackTrace() }
        val completeCallback = { println("Complete!") }
        val s = "Hello World!"

        Observable.just(s).subscribe(successCallback, errorCallback, completeCallback)
        Single.just(s).subscribe(successCallback, errorCallback)
        Completable.fromRunnable { println("Done!!!") }.subscribe(completeCallback, errorCallback)
        Maybe.just(s).subscribe(successCallback, errorCallback, completeCallback)
    }

    /**
     * 测试自制的Observable
     */
    @Test
    fun testSimpleObservable() {
        val observable: Observable<Long> = SimpleObservable("A")
        observable.subscribe(object : SimpleObserver<Long>("B") {
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
                .subscribe(object : SimpleObserver<Long>("B") {
                    override fun onNext(t: Long) {
                        super.onNext(t)
                        if (t == 10L) disposable?.dispose()
                    }
                })
        Thread.sleep(1_000_000)
    }

    /**
     * 创建一个带独立发射数据的发射源
     */
    @Test
    fun testObservableCreate() {
        val emitter = object : ObservableOnSubscribe<Long> {
            private var mObserver: ObservableEmitter<Long>? = null
            override fun subscribe(emitter: ObservableEmitter<Long>) {
                mObserver = emitter
            }

            fun start() {
                Observable.interval(500, TimeUnit.MILLISECONDS)
                        .subscribe {
                            ThreadLogger.log("Emitter send $it")
                            mObserver?.onNext(it)
                        }
            }

            fun next(aLong: Long) {
                mObserver?.onNext(aLong)
            }
        }
        emitter.start()
        Thread.sleep(3000)
        Observable.create(emitter)
                .doOnSubscribe { }
                .subscribe(object : SimpleObserver<Long>("B") {})
        Thread.sleep(1000)
        emitter.next(666)
        Thread.sleep(1000)
        emitter.next(888)

        Thread.sleep(1_000_000)
    }

    /**
     * todo 待测试[Emitter]
     */

    class BleEmitter : ScanCallback(), ObservableOnSubscribe<ScanResult> {
        private var mObserver: ObservableEmitter<ScanResult>? = null
        override fun subscribe(emitter: ObservableEmitter<ScanResult>) {
            mObserver = emitter
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            mObserver?.onNext(result)
        }

        override fun onScanFailed(errorCode: Int) {
            mObserver?.onError(Exception("onScanFailed: $errorCode"))
        }
    }

    /**
     * 直接实现[ObservableSource]并不好，将无法使用[Observable]的各种方法
     */
    class BleEmitter2 : ScanCallback(), ObservableSource<ScanResult> {
        private var mObserver: Observer<in ScanResult>? = null
        override fun subscribe(observer: Observer<in ScanResult>) {
            mObserver = observer
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            mObserver?.onNext(result)
        }

        override fun onScanFailed(errorCode: Int) {
            mObserver?.onError(Exception("onScanFailed: $errorCode"))
        }
    }
}