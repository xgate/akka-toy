package reactive.linkchecker

import akka.actor.{ActorRef, Actor}
import akka.pattern.pipe

class Cache extends Actor {

  import reactive.linkchecker.Cache._
  implicit val exec = context.dispatcher

  var cache = Map.empty[String, String]

  def receive = {
    case Get(url) =>
      if (cache contains url) sender ! cache(url)
      else {
        val client = sender()
        WebClient get url map (Result(client, url, _)) pipeTo self
      }
    case Result(client, url, body) =>
      cache += url -> body
      client ! body
  }
}

object Cache {

  case class Get(url: String)
  case class Result(client: ActorRef, url: String, body: String)
}
