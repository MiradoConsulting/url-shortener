package com.mirado

case class Config(inputFile: String,
                  cassandraHost: String,
                  urlPrefix: String)

object Configuration {

    def readConfig =
        for {
            inputFile <- readConfigVal("INPUT_FILE").right
            cassandraHost <- readConfigVal("CASSANDRA_HOST").right
            urlPrefix <- readConfigVal("URL_PREFIX").right
        } yield Config(inputFile, cassandraHost, urlPrefix)

    private def readConfigVal(key: String) =
        sys.env.get(key) match {
            case None    => Left(new Exception(s"Missing env var: $key"))
            case Some(v) => Right(v)
        }
}

