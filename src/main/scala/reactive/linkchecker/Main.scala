package reactive.linkchecker

import akka.actor.{ReceiveTimeout, Props, Actor}
import scala.concurrent.duration._

/**
 * if you want to run without 'App', Edit Configuration
 * - set 'Main class' = akka.Main
 * - set 'Program arguments' = <your.class.path>
 */
class Main extends Actor {

  import Receptionist._

  val receptionist = context.actorOf(Props[Receptionist], "receptionist")

  receptionist ! Get("http://www.google.com")

  context.setReceiveTimeout(10.seconds)

  def receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Result for '$url':\n", "\n", "\n"))
    case Failed(url) =>
      println(s"Failed to fetch '$url'\n")
    case ReceiveTimeout =>
      context.stop(self)
  }

  override def postStop(): Unit = {
    WebClient.shutdown()
  }
}
