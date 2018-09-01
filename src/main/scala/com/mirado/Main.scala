package com.mirado

import com.mirado.CassandraCluster.withCluster
import com.mirado.CassandraStore.makeCassandraStore
import com.mirado.Configuration.readConfig

import com.datastax.driver.core._
import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.util.Random.alphanumeric

case class Config(inputFile: String, cassandraHost: String)
case class Url (value: String)
case class Hash (value: String)
case class Entry (hash: Hash, url: Url)

object Main {

    implicit val _ = ExecutionContext.global

    def main(args: Array[String]) = {

        val result = for {
            config <- readConfig.right
            result <- withCluster(config, runProgram).right
        } yield result

        result match {
            case Left(e) => System.out.println("Unsuccessful: " + e.getMessage)
            case Right(()) => System.out.println("Success. Exiting.")
        }
    }

    def runProgram(config: Config,
                   session: Session) = {

        val hashGen = () => Hash(alphanumeric take 10 mkString)

        val storage = makeCassandraStore(session, hashGen)

        try {
            for (lines <- fromFile("/home/john/50k").getLines grouped 32) {
                val futures = lines map Url map storage.putOrGet
                futures foreach {x => Await.result(x, 3000 millis)}
            }
            Right ()
        } catch {
            case e: Exception => Left(e)
        }
    }
}
