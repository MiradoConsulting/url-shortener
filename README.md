# url-shortener

## Requirements:
### Cassandra:
A *docker-compose* file is included for this purpose.\
```docker-compose -f cassandra-cluster.yml up```

### Environment Variables:
* `CASSANDRA_HOST`: The location of a cassandra host
* `FRONT_HOST`: The front-facing portion of the URLs to be returned to the user.  e.g. Setting `FRONT_HOST=http://127.0.0.1:8080` means the service will return shortened URLs of the form `http://127.0.0.1:8080/lookup/6UnHLut8NR`

### Scala & sbt:

To run using `sbt`:\
```FRONT_HOST=http://127.0.0.1:8080 CASSANDRA_HOST=172.19.0.2 sbt run```
