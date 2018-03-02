package com.example.rxjavatmp

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DefaultObserver
import io.reactivex.subscribers.DefaultSubscriber
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Created by yupenglei on 18/3/1.
 */

/**
 * 对[Observer]的简单实现，可参考[DefaultObserver]的实现
 */
open class SimpleObserver(private val TAG: String) : Observer<Long> {
    /**
     * 此处不可使用[lateinit var]
     * 因为Observable可能不调用[onSubscribe]函数，如此[disposable]就无法初始化
     */

    protected var disposable: Disposable? = null

    private fun getTag(): String {
        return Thread.currentThread().name + " $TAG"
    }

    init {
        println("${getTag()} hash=${super.hashCode()}")
    }

    override fun onComplete() {
        println("${getTag()} onComplete")
    }

    override fun onSubscribe(d: Disposable) {
        println("${getTag()} ${d.hashCode()} onSubscribe")
        disposable = d
    }

    override fun onNext(t: Long) {
        println("${getTag()} onNext: $t")
    }

    override fun onError(e: Throwable) {
        println("${getTag()} onError: ${e.localizedMessage}")
        e.printStackTrace()
    }
}

/**
 * 对[Subscriber]的简单实现，可参考[DefaultSubscriber]的源码
 */
class SimpleSubscriber(private val TAG: String) : Subscriber<Int> {

    protected var subscription: Subscription? = null
    /**
     * 开始时，会传入[Subscription]，使用[Subscription.request]去请求数据
     * 然后数据才会挨个发送
     */
    override fun onSubscribe(s: Subscription) {
        subscription = s
        s.request(1)
    }

    override fun onComplete() {
        println("$TAG onComplete")
    }

    /**
     * 默认实现：每次请求一个数据
     */
    override fun onNext(t: Int) {
        println("$TAG onNext: $t")
        subscription?.request(1)
    }

    override fun onError(t: Throwable) {
        println("$TAG onError: ${t.localizedMessage}")
        t.printStackTrace()
    }
}