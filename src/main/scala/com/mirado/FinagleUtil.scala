package com.mirado

import com.twitter.{util => twitter}
import scala.concurrent.{ExecutionContext, Promise, Future}
import scala.util.{Failure, Success, Try}

/**
 * Convert Scala Futures into Twitter Futures
 */
object FinagleUtil {

    implicit val _ = ExecutionContext.global

    def scalaToTwitterFuture[T](f: Future[T]): twitter.Future[T] = {
        val promise = twitter.Promise[T]()
        f.onComplete(promise update _)
        promise
    }

    private implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
        case Success(r) => twitter.Return(r)
        case Failure(ex) => twitter.Throw(ex)
    }
}
