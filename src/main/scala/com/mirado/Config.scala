package com.mirado

case class Config(cassandraHost: String,
                  frontHost: String)

object Configuration {

    def readConfig =
        for {
            cassandraHost <- readConfigVal("CASSANDRA_HOST").right
            frontHost <- readConfigVal("FRONT_HOST").right
        } yield Config(cassandraHost, frontHost)

    private def readConfigVal(key: String) =
        sys.env.get(key) match {
            case None    => Left(new Exception(s"Missing env var: $key"))
            case Some(v) => Right(v)
        }
}
