package com.mirado

import com.mirado.CassandraCluster.withCluster

import com.datastax.driver.core._
import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.util.Random.alphanumeric

case class Url (value: String)
case class Hash (value: String)
case class Entry (hash: Hash, url: Url)

object Main {

    def main(args: Array[String]) =

        withCluster("172.19.0.2", "ks_url_shortener", runProgram) match {

            case Left(e) =>
                System.out.println("Unsuccessful: " + e.getMessage)

            case Right(()) =>
                System.out.println("Success. Exiting.")
        }

    def runProgram(session: Session): Either[Exception, Unit] = {

        implicit val _ = ExecutionContext.global

        val cassandra = CassandraStore.construct(session)

        val hashGen = () => Hash(alphanumeric take 10 mkString)

        val storage = new Storage(store     = session,
                                  getByUrl  = cassandra.lookupByUrl,
                                  getByHash = cassandra.lookupByHash,
                                  put       = cassandra.store,
                                  genHash   = hashGen)

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
