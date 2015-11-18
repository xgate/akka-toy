package cluster.singleton

import akka.actor.{ActorLogging, Actor}
import SingletonActor._

class SingletonActor extends Actor with ActorLogging {

  log.info("singleton actor started...!")

  override def receive: Receive = {
    case Ping =>
      log.info("received ping message..!")
    case _ =>
      log.info("received unknown message...")
  }
}

object SingletonActor {

  case object Ping
}
