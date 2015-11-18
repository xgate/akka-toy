package cluster.singleton

import akka.actor.ActorSystem
import akka.contrib.pattern.ClusterSingletonProxy
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
 * 주기적으로 메시지 보낸다.
 */
object Sender extends App {

  val config = ConfigFactory.load()
    .getConfig("cluster")
    .withOnlyPath("akka")
    .withFallback(ConfigFactory.load())
  val customConf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 0)
    .withFallback(config)

  // system name 맞춰주기
  val system = ActorSystem("ClusterSystem", customConf)
  val singletonActorName = "singleton-actor"
  val singletonName = "singleton-worker"

  val proxy = system.actorOf(ClusterSingletonProxy.props(
    singletonPath = s"/user/$singletonActorName/$singletonName",
    role = Some("seed")), name = "proxy")

  import system.dispatcher
  system.scheduler.schedule(1.seconds, 5.seconds) {
    proxy ! SingletonActor.Ping
  }
}
