package com.example.rxjavatmp

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by yupenglei on 18/3/1.
 */
open class SimpleObserver(private val tag: String) : Observer<Long> {
    init {
        println("$tag hash=${hashCode()}")
    }

    override fun onComplete() {
        println("$tag onComplete")
    }

    override fun onSubscribe(d: Disposable) {
        println("$tag ${d.hashCode()} onSubscribe")
    }

    override fun onNext(t: Long) {
        println("$tag onNext: $t")
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }
}