package com.mirado

case class Config(cassandraHost: String,
                  urlPrefix: String)

object Configuration {

    def readConfig =
        for {
            cassandraHost <- readConfigVal("CASSANDRA_HOST").right
            urlPrefix <- readConfigVal("URL_PREFIX").right
        } yield Config(cassandraHost, urlPrefix)

    private def readConfigVal(key: String) =
        sys.env.get(key) match {
            case None    => Left(new Exception(s"Missing env var: $key"))
            case Some(v) => Right(v)
        }
}

