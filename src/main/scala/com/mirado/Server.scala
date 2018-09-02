package com.mirado

import com.mirado.FinagleUtil.scalaToTwitterFuture

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.path.{Path, Root, /, ->}
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.{Request, Response, Status, ParamMap}
import com.twitter.util.{Await, Future}
import scala.concurrent.ExecutionContext

class UrlShortenService[Store](config: Config, store: Storage[Store]) extends Service[Request, Response] {

    implicit val _ = ExecutionContext.global

    def apply(req: Request): Future[Response] =

        req.method -> Path(req.path) match {

            case Get -> Root / "lookup" / hash  =>
                lookup(Hash(hash))

            case Get -> Root / "check" / hash =>
                check(Hash(hash))

            case Post -> Root / "create" =>
                req.params.get("url") match {
                    case Some(url) => create(Url(url))
                    case None => missingUrlInCreate()
                }

            case _ => notFound()
        }

    private def lookup(hash: Hash) = scalaToTwitterFuture {
        store.lookup(hash) map {
            case Some(Entry(hash, url)) => {
                val response = Response(Status.SeeOther)
                response.headerMap.put("location", url.value)
                response
            }
            case None => {
                val response = Response(Status.NotFound)
                response.setContentString(s"hash ${hash.value} not found")
                response
            }
        }
    }

    // Just return the hash instead of redirecting
    private def check(hash: Hash) = scalaToTwitterFuture {
        store.lookup(hash) map {
            case Some(Entry(hash, url)) => {
                val response = Response(Status.Ok)
                response.setContentString(url.value)
                response
            }
            case None => {
                val response = Response(Status.NotFound)
                response.setContentString(s"hash ${hash.value} not found")
                response
            }
        }
    }

    private def create(url: Url) = scalaToTwitterFuture {
        store.putOrGet(url) map {
            case Left(_) => {
                val response = Response(Status.InternalServerError)
                response.setContentString(s"Unable to store url: ${url.value}")
                response
            }
            case Right(entry: Entry) => {
                val response = Response(Status.Ok)
                response.setContentString(s"${config.frontHost}/lookup/${entry.hash.value}")
                response
            }
        }
    }

    private def missingUrlInCreate() = Future {
        val response = Response(Status.BadRequest)
        response.setContentString("Missing 'url' parameter in POST request")
        response
    }

    private def notFound() = Future {
        val response = Response(Status.NotFound)
        response.setContentString("Route not found")
        response
    }
}

object UrlShortenService {
    def runServer[Store](config: Config, store: Storage[Store]): Unit = { 
       val service = new UrlShortenService(config, store)
       val server = Http.serve(":8080", service)
       Await.ready(server)
    }
}
