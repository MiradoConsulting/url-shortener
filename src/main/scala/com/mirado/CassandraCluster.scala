package com.mirado

import com.datastax.driver.core._

object CassandraCluster {

    def withCluster[A](contact: String,
                       keyspace: String,
                       action: Session => Either [Exception, A]) = {

        val cluster = prepareCluster(contact)
        try {
            val session = cluster.connect()
            prepareKeyspace(session, keyspace)
            prepareTable(session)
            createIndex(session)
            action(session)
        } catch {
            case e: Exception => Left(e)
        } finally {
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    private def prepareCluster(contact: String) =
        Cluster.builder()
               .addContactPoint(contact)
               .build()

    private def prepareKeyspace(session: Session, keyspace: String) =
        session.execute(
            s"""
                CREATE KEYSPACE IF NOT EXISTS ks_url_shortener
                WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 2}
                AND DURABLE_WRITES = true
            """
        )

    private def prepareTable(session: Session) =
        session.execute(
            s"""
                CREATE TABLE IF NOT EXISTS ks_url_shortener.url_to_hash
                (url text PRIMARY KEY, hash ascii);
            """
        )

    private def createIndex(session: Session) =
        session.execute(
            s"""
                CREATE INDEX IF NOT EXISTS idx_hash
                ON ks_url_shortener.url_to_hash (hash);
            """
        )
}
