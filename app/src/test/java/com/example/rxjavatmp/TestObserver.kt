package com.example.rxjavatmp

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.internal.observers.DisposableLambdaObserver
import io.reactivex.observers.*
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by yupenglei on 18/2/26.
 */
class TestObserver {
    /**
     * 观察者；接收数据流
     * 列举所有Observer
     */
    @Test
    fun testObserver() {
        //===========================================================================
        // 一级
        /**
         * 顶级接口[Observer]
         */
        val observer: Observer<Int> = object : Observer<Int> {
            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {}
            override fun onNext(t: Int) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * 顶级接口[SingleObserver]
         */
        object : SingleObserver<Int> {
            override fun onSuccess(t: Int) {}
            override fun onSubscribe(d: Disposable) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * 顶级接口[CompletableObserver]
         */
        object : CompletableObserver {
            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * 顶级接口[MaybeObserver]
         */
        object : MaybeObserver<Int> {
            override fun onSuccess(t: Int) {}
            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {}
            override fun onError(e: Throwable) {}
        }

        //===========================================================================
        // 二级
        /**
         * [Observer]的直接抽象子类
         */
        object : DefaultObserver<Int>() {
            override fun onComplete() {}
            override fun onNext(t: Int) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [Observer]的直接抽象子类
         */
        object : ResourceObserver<Int>() {
            override fun onComplete() {}
            override fun onNext(t: Int) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [Observer]的直接抽象子类
         */
        object : DisposableObserver<Int>() {
            override fun onComplete() {}
            override fun onNext(t: Int) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [Observer]的直接子类
         */
        DisposableLambdaObserver<Int>(observer,
                { println("onSubscribe:" + it) },
                { println("onDispose") })

        //===========================================================================
        // 三级
        //所有的ResourceXXXObserver均继承了Disposable接口
        /**
         * [SingleObserver]的直接子类
         */
        object : ResourceSingleObserver<Int>() {
            override fun onSuccess(t: Int) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [CompletableObserver]的直接子类
         */
        object : ResourceCompletableObserver() {
            override fun onComplete() {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [MaybeObserver]的直接子类
         */
        object : ResourceMaybeObserver<Int>() {
            override fun onSuccess(t: Int) {}
            override fun onComplete() {}
            override fun onError(e: Throwable) {}
        }

        //===========================================================================
        // 三级
        //所有的DisposableXXXObserver均继承了Disposable接口
        /**
         * [SingleObserver]的直接子类
         */
        object : DisposableSingleObserver<Int>() {
            override fun onSuccess(t: Int) {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [CompletableObserver]的直接子类
         */
        object : DisposableCompletableObserver() {
            override fun onComplete() {}
            override fun onError(e: Throwable) {}
        }

        /**
         * [MaybeObserver]的直接子类
         */
        object : DisposableMaybeObserver<Int>() {
            override fun onSuccess(t: Int) {}
            override fun onComplete() {}
            override fun onError(e: Throwable) {}
        }
    }

    /**
     * 测试多个观察者订阅一个数据源
     * 结果：每个观察者订阅时，才会新建一个数据源进行发射，所以实际是每个观察者分别订不同的新建数据源
     * 真实原因：每次订阅时都会调用[Observable]的subscribeActual函数，在该函数内进行了新interval线程的创建执行，
     * 所以若想数据源不变，则不应将数据源的创建和发射放在subscribeActual函数中，应仅在该函数内进行[Observer]的保存
     * 详情见自定义的[AdvanceObservable]
     */
    @Test
    fun testMultiObserver() {
        val interval = Observable.interval(500, TimeUnit.MILLISECONDS)
        interval.subscribe(SimpleObserver("A"))
        Thread.sleep(1_000)
        interval.subscribe(SimpleObserver("B"))
        Thread.sleep(10_000)
    }
}