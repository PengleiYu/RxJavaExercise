package com.example.rxjavatmp

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Created by yupenglei on 18/3/1.
 */
open class SimpleObserver(private val TAG: String) : Observer<Long> {
    init {
        println("$TAG hash=${super.hashCode()}")
    }

    override fun onComplete() {
        println("$TAG onComplete")
    }

    override fun onSubscribe(d: Disposable) {
        println("$TAG ${d.hashCode()} onSubscribe")
    }

    override fun onNext(t: Long) {
        println("$TAG onNext: $t")
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }
}

class SimpleSubscriber(private val TAG: String) : Subscriber<Int> {
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