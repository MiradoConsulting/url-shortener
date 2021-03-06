package com.mirado.store

import com.datastax.driver.core._
import com.google.common.util.concurrent._
import scala.concurrent._

/**
 * Convert Cassandra Futures into Scala Futures
 */
object CassandraUtil {

    def convertFuture(future: ResultSetFuture) = {
        val promise = Promise[ResultSet]()
        val callback = new FutureCallback[ResultSet] {
            def onSuccess(result: ResultSet): Unit = promise success result
            def onFailure(err: Throwable): Unit = promise failure err
        }
        Futures.addCallback(future, callback)
        promise.future
    }
}
