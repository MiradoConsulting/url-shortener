package com.mirado

import com.datastax.driver.core._
import scala.concurrent._

class CassandraStore(insertByHash: PreparedStatement,
                     insertByUrl: PreparedStatement,
                     queryByUrl: PreparedStatement,
                     queryByHash: PreparedStatement) {

    import CassandraUtil.convertFuture

    implicit val _ = ExecutionContext.global

    def store(session: Session, hash: Hash, url: Url) =
        convertFuture(session.executeAsync(insertByHash.bind(hash.value, url.value))) flatMap {
            byHashResult => 
                if(byHashResult.wasApplied) {
                    convertFuture(session.executeAsync(insertByUrl.bind(url.value, hash.value))) map {
                        byUrlResult => Some(Entry(hash, url)) filter {_ => byUrlResult.wasApplied }
                    }
                } else {
                    Future(None)
                }
        }

    def lookupByUrl(session: Session, url: Url) =
        convertFuture(session.executeAsync(queryByUrl.bind(url.value))) map {
            results => Some(results.one())
                           .filter {r => r != null}
                           .map {r => Entry(Hash(r.getString("hash")),
                                            Url(r.getString("url")))}
        }

    def lookupByHash(session: Session, hash: Hash) =
        convertFuture(session.executeAsync(queryByHash.bind(hash.value))) map {
            results => Some(results.one())
                           .filter {r => r != null}
                           .map {r => Entry(Hash(r.getString("hash")),
                                            Url(r.getString("url")))}
        }
}

object CassandraStore {

    def makeCassandraStore(session: Session,
                           genHash: () => Hash) = {

        val cassandra = construct(session)

        new Storage(store     = session,
                    getByUrl  = cassandra.lookupByUrl,
                    getByHash = cassandra.lookupByHash,
                    put       = cassandra.store,
                    genHash   = genHash)
    }

    private def construct(session: Session) = {

        val cqlInsertByHash = s"""
                                INSERT INTO url_shortener.hash_to_url (hash, url)
                                VALUES (?, ?)
                                IF NOT EXISTS;
                              """

        val cqlInsertByUrl = s"""
                               INSERT INTO url_shortener.url_to_hash (url, hash)
                               VALUES (?, ?)
                               IF NOT EXISTS;
                             """

        val cqlQueryByUrl = s"""
                              SELECT * FROM url_shortener.url_to_hash
                              WHERE url = ?
                              LIMIT 1;
                            """

        val cqlQueryByHash = s"""
                              SELECT * FROM url_shortener.hash_to_url
                              WHERE hash = ?
                              LIMIT 1;
                            """

        new CassandraStore(insertByHash = session.prepare(cqlInsertByHash),
                           insertByUrl  = session.prepare(cqlInsertByUrl),
                           queryByUrl   = session.prepare(cqlQueryByUrl),
                           queryByHash  = session.prepare(cqlQueryByHash))
    }
}
