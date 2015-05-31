package reactive.linkchecker

import akka.actor.{Props, ActorRef, Actor}
import reactive.linkchecker.Receptionist.{Result, Get, Failed}

class Receptionist extends Actor {

  def receive = waiting

  case class Job(client: ActorRef, url: String)
  var reqNo = 0

  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if (queue.isEmpty) waiting
    else {
      val controller = context.actorOf(Props[Controller], s"c$reqNo")
      controller ! Controller.Check(queue.head.url, 2)
      running(queue)
    }
  }

  def enqueueJob(queue: Vector[Job], job: Job): Receive = {
    if (queue.size > 3) {
      sender ! Failed(job.url)
      running(queue)
    } else running(queue :+ job)
  }

  val waiting: Receive = {
    // upon Get(url) start a traversal and become running
    case Get(url) => context.become(runNext(Vector(Job(sender(), url))))
  }

  def running(queue: Vector[Job]): Receive = {
    // upon Get(url) append that to queue and keep running
    // upon Controller.Result(links) ship that to client
    // and run next job from queue (if any)
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      context.stop(sender())
      context.become(runNext(queue.tail))
    case Get(url) =>
      context.become(enqueueJob(queue, Job(sender(), url)))
  }
}

object Receptionist {
  case class Failed(url: String)
  case class Get(url: String)
  case class Result(url: String, links: Set[String])
}
