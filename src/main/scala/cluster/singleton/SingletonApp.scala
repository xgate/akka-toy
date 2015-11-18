package cluster.singleton

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.contrib.pattern.ClusterSingletonManager
import com.typesafe.config.ConfigFactory

/**
 * Ref. https://github.com/Synesso/scratch-akka-cluster-singleton
 */
object SingletonApp {

  val seedPorts = Set("2551", "2552")

  def main(args: Array[String]): Unit = {
    startup(args.headOption.getOrElse("0"))
  }

  def startup(port: String) = {
    val config = ConfigFactory.load()
      .getConfig("cluster")
      .withOnlyPath("akka")
      .withFallback(ConfigFactory.load())
    val customConf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
      .withFallback(config)

    // Create an Akka system
    val system = ActorSystem("ClusterSystem", customConf)

    println(customConf.getInt("akka.remote.netty.tcp.port"))

    val clusterSingletonProps = ClusterSingletonManager.props(
      singletonProps = Props(classOf[SingletonActor]),
      singletonName = "singleton-worker",
      terminationMessage = PoisonPill,
      role = Some("seed"))

    system.actorOf(clusterSingletonProps, "singleton-actor")
  }
}
