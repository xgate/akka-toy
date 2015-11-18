package cluster.transformation

import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

/**
 * backend actor들을 소중히 간직한다. job이 오면 backend로 던진다.
 */
class TransformationFrontend extends Actor {

  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  def receive = {
    case job: TransformationJob if backends.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later", job)

    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size) forward job

    case BackendRegistration if !backends.contains(sender()) =>
      context watch sender()
      backends = backends :+ sender()

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}

object TransformationFrontend {
  def main (args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(
      s"""
         |akka {
         |    actor {
         |      provider = "akka.cluster.ClusterActorRefProvider"
         |    }
         |    remote {
         |      log-remote-lifecycle-events = off
         |      netty.tcp {
         |        hostname = "127.0.0.1"
         |        port = $port
         |      }
         |    }
         |    cluster {
         |      roles = ["frontend"]
         |      seed-nodes = [
         |        "akka.tcp://ClusterSystem@127.0.0.1:2551",
         |        "akka.tcp://ClusterSystem@127.0.0.1:2552"]
         |      auto-down-unreachable-after = 10s
         |    }
         |}
       """.stripMargin).withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[TransformationFrontend], name = "frontend")

    val counter = new AtomicInteger
    import system.dispatcher
    system.scheduler.schedule(2.seconds, 2.seconds) {
      implicit val timeout = Timeout(5.seconds)
      (frontend ? TransformationJob("hello-" + counter.incrementAndGet())) onSuccess {
        case result => println(result)
      }
    }
  }
}
