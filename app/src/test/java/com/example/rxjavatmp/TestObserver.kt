package com.example.rxjavatmp

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.internal.observers.DisposableLambdaObserver
import io.reactivex.observers.*
import io.reactivex.subscribers.DefaultSubscriber
import io.reactivex.subscribers.DisposableSubscriber
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by yupenglei on 18/2/26.
 */
class TestObserver {
    private val completeBlock: () -> Unit = { println("onCompleted") }
    private val errorBlock: (Throwable) -> Unit = { println(it) }
//    private val nextBlock: (Any) -> Unit = { println(it) }

    @Test
    fun testFlowable() {
        Flowable.just("Hello world!")
                .subscribe { println(it) }
    }

    @Test
    fun testObservable() {
        Observable.just("Hello world!")
                .subscribe { println(it) }
    }

    @Test
    fun testSingle() {
        Single.just("Hello world!")
                .subscribe({ println(it) }, errorBlock)
    }

    @Test
    fun testCompletable() {
        Completable.fromCallable { }
                .subscribe(completeBlock, errorBlock)
    }

    @Test
    fun testMayBe() {
        Maybe.just("Hello world!")
                .subscribe({ println(it) }, errorBlock, completeBlock)
    }

    /**
     * 观察者；接收数据流
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

    @Test
    fun test1() {
        Observable
//                .just(1L, 2, 3, 4, 5)
                .interval(1, TimeUnit.SECONDS)
                .subscribe(object : Observer<Long> {
                    override fun onComplete() {
                        println("onComplete")
                    }

                    override fun onSubscribe(d: Disposable) {
                        println("onSubscribe")
                    }

                    override fun onNext(t: Long) {
                        println("onNext: $t")
                    }

                    override fun onError(e: Throwable) {
                        println("onError: ${e.localizedMessage}")
                    }
                })
        Thread.sleep(10_000)
    }
}