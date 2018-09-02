# url-shortener

## Requirements:
### Cassandra:
A *docker-compose* file is included for this purpose.\
```docker-compose -f cassandra-cluster.yml up```

### Environment Variables:
* `CASSANDRA_HOST`: The location of a cassandra host
* `FRONT_HOST`: The front-facing portion of the URLs to be returned to the user.\
e.g. Setting `FRONT_HOST=http://127.0.0.1:8080` means the service will return shortened URLs of the form `http://127.0.0.1:8080/lookup/6UnHLut8NR`

### Scala & sbt:

To run using `sbt`:\
```FRONT_HOST=http://127.0.0.1:8080 CASSANDRA_HOST=172.19.0.2 sbt run```

### To use:
Creating a shortened URL:
```
$ curl -X POST http://127.0.0.1:8080/create -d"url=http://www.scala-lang.org" && echo

http://127.0.0.1:8080/lookup/bUEgpuOrUg
```

Accessing a shortened URL:
```
$ curl -v http://127.0.0.1:8080/lookup/bUEgpuOrUg

*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
> GET /lookup/bUEgpuOrUg HTTP/1.1
> Host: 127.0.0.1:8080
> User-Agent: curl/7.58.0
> Accept: */*
> 
< HTTP/1.1 303 See Other
< location:  http://www.scala-lang.org
< Content-Length: 0
< 
* Connection #0 to host 127.0.0.1 left intact
```
