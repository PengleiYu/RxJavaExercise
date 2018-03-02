package com.example.rxjavatmp

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * RxJava的一些实际用例
 * Created by yupenglei on 18/3/2.
 */
class ActualUsage {

    /**
     * 用一个数据请求多个不同的接口，如何统一处理（各接口返回相同数据类型）
     */
    @Test
    fun testUsage1() {
        Observable.just(Headers())
                .flatMap { Observable.merge(categoryRequest(it), sellerRequest(it)) }
                .observeOn(Schedulers.computation())
                .subscribe(SimpleObserver("A"))
    }

    class Headers
    data class ResCommendEntity(val id: Int)

    private fun categoryRequest(headers: Headers): Observable<ResCommendEntity> {
        return Observable.timer(1, TimeUnit.SECONDS)
                .map { ResCommendEntity(1) }
    }

    private fun sellerRequest(headers: Headers): Observable<ResCommendEntity> {
        return Observable.just(ResCommendEntity(2))
    }
}
