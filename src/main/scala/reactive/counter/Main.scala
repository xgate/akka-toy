package reactive.counter

import akka.actor.{ActorSystem, Props, Actor}

class Main extends Actor {

  val counter = context.actorOf(Props[Counter], "counter")

  counter ! "incr"
  counter ! "incr"
  counter ! "incr"

  counter ! "get"

  def receive = {
    case count: Int =>
      println(s"count was $count")
      context.stop(self)
  }
}

object Main extends App {

  val system = ActorSystem("counter-system")

  system.actorOf(Props[Main], "counter-main")
}
