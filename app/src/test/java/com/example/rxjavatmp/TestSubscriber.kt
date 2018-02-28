package com.example.rxjavatmp

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableSubscriber
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DefaultSubscriber
import io.reactivex.subscribers.DisposableSubscriber
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

/**
 * Created by yupenglei on 18/2/27.
 */
class TestSubscriber {
    /**
     * 列举[Subscriber]相关接口和子类
     */
    @Test
    fun testSubscriber() {
        //===========================================================================
        /**
         * 顶级接口[Subscriber]
         */
        object : Subscriber<Int> {
            override fun onComplete() {}
            override fun onSubscribe(s: Subscription?) {}
            override fun onNext(t: Int?) {}
            override fun onError(t: Throwable?) {}
        }
        //===========================================================================
        /**
         * [Subscriber]的二级子接口
         * 和[Subscriber]的区别就是[Subscriber.onSubscribe]的参数改为了非null
         */
        object : FlowableSubscriber<Int> {
            override fun onComplete() {}
            override fun onSubscribe(s: Subscription) {}
            override fun onNext(t: Int?) {}
            override fun onError(t: Throwable?) {}
        }
        //===========================================================================
        /**
         * [FlowableSubscriber]的二级子接口
         * 三者区别参见[testSubscriberDiff]
         */
        object : DefaultSubscriber<Int>() {
            override fun onStart() {}
            override fun onComplete() {}
            override fun onNext(t: Int?) {}
            override fun onError(t: Throwable?) {}
        }
        object : ResourceSubscriber<Int>() {
            override fun onStart() {}
            override fun onComplete() {}
            override fun onNext(t: Int?) {}
            override fun onError(t: Throwable?) {}
        }
        object : DisposableSubscriber<Int>() {
            override fun onStart() {}
            override fun onComplete() {}
            override fun onNext(t: Int?) {}
            override fun onError(t: Throwable?) {}
        }
    }

    /**
     * 用于匿名对象的打印
     */
    interface TagInterface {
        val tag: String
    }

    /**
     * 本方法测试三种[Subscriber]默认实现的差异
     * [DefaultSubscriber], [ResourceSubscriber], [DisposableSubscriber]
     */
    @Test
    fun testSubscriberDiff() {
        val defaultSubscriber: DefaultSubscriber<Int> = object : DefaultSubscriber<Int>(), TagInterface {
            /**
             * 默认的[onStart]是请求[Long.MAX_VALUE]
             * 需进行自定义行为
             */
            override fun onStart() {
                request(1)
            }

            override fun onComplete() {
                println("$tag onComplete")
            }

            /**
             * 每次进行拉取数据[request]，不需要是可以[cancel]
             * [cancel]取消本次订阅，数据源还在发射数据
             */
            override fun onNext(t: Int) {
                println("$tag onNext: $t")
                if (t == 7) cancel()
                request(1)
            }

            override fun onError(t: Throwable) {
                t.printStackTrace()
            }

            override val tag: String
                get() = "DefaultSubscriber"
        }
        /**
         * 实现了[Disposable]接口，可在外部调用[Disposable.dispose]进行终止
         * 内部持有多个数据源
         */
        val resourceSubscriber: ResourceSubscriber<Int> = object : ResourceSubscriber<Int>(), TagInterface {
            /**
             * [ResourceSubscriber]可以订阅多个数据源
             * [add]用于添加数据源
             * [onStart]默认[request] [Long.MAX_VALUE]
             */
            override fun onStart() {
                add(Schedulers.single()
                        .scheduleDirect({ println("$tag Time!") }, 2, TimeUnit.SECONDS))
                request(1)
            }

            override fun onComplete() {
                println("$tag onComplete")
                dispose()
            }

            /**
             * [dispose]释放所有资源
             */
            override fun onNext(t: Int) {
                println("$tag onNext: $t")
                if (t == 5) dispose()
                request(1)
            }

            override fun onError(t: Throwable) {
                t.printStackTrace()
                dispose()
            }

            override val tag: String
                get() = "ResourceSubscriber"
        }
        /**
         * [DisposableSubscriber]和[DefaultSubscriber]基本相同
         * 区别在于[DisposableSubscriber]多实现了[Disposable]接口
         * 可在外部调用[Disposable.dispose]进行终止，而各个子类的cancel方法均是protect的
         */
        val disposableSubscriber: DisposableSubscriber<Int> = object : DisposableSubscriber<Int>(), TagInterface {
            override fun onStart() {
                request(1)
            }

            override fun onComplete() {
                println("$tag onComplete")
            }

            override fun onNext(t: Int) {
                println("$tag onNext: $t")
                if (t == 5) cancel()
                request(1)
            }

            override fun onError(t: Throwable) {
                println("$tag onError ${t.localizedMessage}")
            }

            override val tag: String
                get() = "DisposableSubscriber"
        }

        val flowable = Flowable
                .create<Int>({
                    for (i in 0..100) {
                        it.onNext(i)
                        //todo 每个订阅者都会导致调用一次
                        println("Emitter ${it.hashCode()} onNext: $i")
                        Thread.sleep(500)
                    }
                }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.computation())
        flowable.subscribe(defaultSubscriber)
        Thread.sleep(1_000)//可以发现每次订阅都会重新创建一个数据源
        flowable.subscribe(resourceSubscriber)
        val subscriber = flowable.subscribeWith(disposableSubscriber)
        Thread.sleep(1_000)
        subscriber.dispose()//取消订阅导致数据源的sleep出现打断异常
        Thread.sleep(10_000)
    }

    /**
     * 测试[Flowable], [Subscriber]的创建及建立订阅关系
     */
    @Test
    fun testCreateSubscription() {
        /**
         * 如何创建自己的Flowable：
         * 创建一个lambda，然后调用发射器进行发射
         * 将该lambda传入create方法
         */
        val source: (FlowableEmitter<Int>) -> Unit = {
            for (i in 0..100) {
                it.onNext(i)
                Thread.sleep(500)
            }
        }

        class MySubscriber(private val TAG: String) : Subscriber<Int> {
            /**
             * 开始时，会传入[Subscription]，使用[Subscription.request]去请求数据
             * 然后数据才会挨个发送
             */
            override fun onSubscribe(s: Subscription) {
                s.request(5)
            }

            override fun onComplete() {
                println("$TAG onComplete")
            }

            override fun onNext(t: Int) {
                println("$TAG onNext: $t")
            }

            override fun onError(t: Throwable) {
                println("$TAG onError: ${t.localizedMessage}")
            }
        }

        val flowable = Flowable.create(source, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.computation())
        /**
         * A和B获取的值相同
         */
        flowable.subscribe(MySubscriber("A"))
        flowable.subscribe(MySubscriber("B"))
        Thread.sleep(10_000)
    }
}