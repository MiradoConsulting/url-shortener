package com.mirado.store

import com.mirado.{Entry, Url, Hash}
import com.mirado.util.Retry.retry

import scala.concurrent._

class Storage[Store](store:     Store,
                     getByUrl:  (Store, Url)       => Future[Option[Entry]],
                     getByHash: (Store, Hash)      => Future[Option[Entry]],
                     put:       (Store, Hash, Url) => Future[Option[Entry]],
                     genHash:   ()                 => Hash) {

    implicit val _ = ExecutionContext.global

    def putOrGet(url: Url) =

        getByUrl(store, url) flatMap {

            case Some(entry) =>
                Future(Right(entry))

            case None =>
                retry(3, None, () => put(store, genHash(), url))
        }

    def lookup(hash: Hash) = getByHash(store, hash)
}
