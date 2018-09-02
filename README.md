# url-shortener

## Requirements:
### Cassandra:
A *docker-compose* file is included for this purpose.\
```docker-compose -f cassandra-cluster.yml up```

### Environment Variables:
* `CASSANDRA_HOST`: The location of a cassandra host
* `URL_PREFIX`: The hostname this service is expected to run as.

### Scala & sbt:

To run using `sbt`:\
```URL_PREFIX="http://127.0.0.1:8080/lookup/" CASSANDRA_HOST=172.19.0.2 sbt run```
