package com.mirado

import com.mirado.service.UrlShortenService.runServer
import com.mirado.store.CassandraCluster.withCluster
import com.mirado.store.CassandraStore.makeCassandraStore
import com.mirado.util.Config

import com.datastax.driver.core.Session
import scala.util.Random.alphanumeric

object Main {

    def main(args: Array[String]) = {

        val result = for {
            config <- Config.readConfig.right
            result <- withCluster(config, run).right
        } yield result

        result match {
            case Left(e) => System.out.println(s"Unsuccessful: ${e.getMessage}")
            case Right(()) => System.out.println("Success. Exiting.")
        }
    }

    private def run(config: Config, session: Session) =
        try {
            val hashGen = () => Hash(alphanumeric take 10 mkString)
            val storage = makeCassandraStore(session, hashGen)
            runServer(config, storage)
            Right(())
        } catch {
            case e: Exception => Left(e)
        }
}
