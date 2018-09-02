package com.mirado.util

import scala.concurrent._

object Retry {

    implicit val _ = ExecutionContext.global

    def retry[T,F](retries: Integer,
                   fail: F,
                   attempt: () => Future[Option[T]]): Future[Either[F, T]] =

        if(retries <= 0) {
            Future(Left(fail))
        } else {
            attempt() flatMap {
                case None => retry(retries-1, fail, attempt)
                case Some(x) => Future(Right(x))
            }
        }
}
