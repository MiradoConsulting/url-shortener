package com.mirado

object Configuration {

    def readConfig =
        for {
            inputFile <- readConfigVal("INPUT_FILE").right
            cassandraHost <- readConfigVal("CASSANDRA_HOST").right
        } yield Config(inputFile, cassandraHost)

    private def readConfigVal(key: String) =
        sys.env.get(key) match {
            case None    => Left(new Exception(s"Missing env var: $key"))
            case Some(v) => Right(v)
        }
}

