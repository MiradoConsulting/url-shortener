package com.mirado.store

import com.mirado.util.Config

import com.datastax.driver.core._

object CassandraCluster {

    def withCluster[A](config: Config,
                       action: (Config, Session) => Either [Exception, A]) = {

        val cluster = prepareCluster(config.cassandraHost)
        try {
            val session = cluster.connect()
            prepareKeyspace(session)
            prepareTables(session)
            action(config, session)
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

    private def prepareKeyspace(session: Session) =
        session.execute(
            s"""
                CREATE KEYSPACE IF NOT EXISTS url_shortener
                WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 2}
                AND DURABLE_WRITES = true
            """
        )

    private def prepareTables(session: Session) = {

        session.execute(
            s"""
                CREATE TABLE IF NOT EXISTS url_shortener.hash_to_url
                (hash ascii PRIMARY KEY, url text);
            """
        )

        session.execute(
            s"""
                CREATE TABLE IF NOT EXISTS url_shortener.url_to_hash
                (url text PRIMARY KEY, hash ascii);
            """
        )
    }
}
