package cluster.transformation

import akka.actor.{Props, ActorSystem, RootActorPath, Actor}
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import com.typesafe.config.ConfigFactory

/**
 * 뒤에서 묵묵히 자기 일 수행
 */
class TransformationBackend extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case TransformationJob(text) => sender() ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! BackendRegistration
}

object TransformationBackend {
  def main(args: Array[String]): Unit = {
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
         |      roles = ["backend"]
         |      seed-nodes = [
         |        "akka.tcp://ClusterSystem@127.0.0.1:2551",
         |        "akka.tcp://ClusterSystem@127.0.0.1:2552"]
         |      auto-down-unreachable-after = 10s
         |    }
         |}
       """.stripMargin).withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[TransformationBackend], name = "backend")
  }
}
