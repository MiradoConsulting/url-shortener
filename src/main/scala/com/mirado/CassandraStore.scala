package com.mirado

import com.datastax.driver.core._
import scala.concurrent._

class CassandraStore(insert: PreparedStatement,
                     queryByUrl: PreparedStatement,
                     queryByHash: PreparedStatement) {

    import CassandraUtil.convertFuture

    implicit val _ = ExecutionContext.global

    def store(session: Session, hash: Hash, url: Url) =
        convertFuture(session.executeAsync(insert.bind(url.value, hash.value)))
            .map {results => Some(Entry(hash, url))
                                 .filter {_ => results.wasApplied}}

    def lookupByUrl(session: Session, url: Url) =
        convertFuture(session.executeAsync(queryByUrl.bind(url.value)))
            .map {results => Some(results.one())
                                 .filter {r => r != null}
                                 .map {r => Entry(Hash(r.getString("hash")),
                                                   Url(r.getString("url")))}}

    def lookupByHash(session: Session, hash: Hash) =
        convertFuture(session.executeAsync(queryByHash.bind(hash.value)))
            .map {results => Some(results.one())
                                 .filter {r => r != null}
                                 .map {r => Entry(Hash(r.getString("hash")),
                                                  Url(r.getString("url")))}}
}

object CassandraStore {

    def construct(session: Session) = {

        val cqlInsert = s"""
                          INSERT INTO ks_url_shortener.url_to_hash (url, hash)
                          VALUES (?, ?)
                          IF NOT EXISTS;
                        """

        val cqlQueryByUrl = s"""
                              SELECT * FROM ks_url_shortener.url_to_hash
                              WHERE url = ?
                              LIMIT 1;
                            """

        val cqlQueryByHash = s"""
                              SELECT * FROM ks_url_shortener.url_to_hash
                              WHERE hash = ?
                              LIMIT 1;
                            """

        new CassandraStore(insert      = session.prepare(cqlInsert),
                           queryByUrl  = session.prepare(cqlQueryByUrl),
                           queryByHash = session.prepare(cqlQueryByHash))
    }
}
