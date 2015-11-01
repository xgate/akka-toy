package persistence

import akka.actor.{Props, ActorSystem}
import akka.persistence.{SnapshotOffer, PersistentActor}

case class Cmd(data: String)
case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString()
}

class ExamplePersistentActor extends PersistentActor {

  override def persistenceId = "sample-id-1"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents = state.size

  override def receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
  }

  override def receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"$data-$numEvents"))(updateState)
      persist(Evt(s"$data-${numEvents + 1}")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
      }
    case "snap" => saveSnapshot(state)
    case "print" => println(state)
  }
}

object ExamplePersistentActor {

  def props = Props[ExamplePersistentActor]
}

object ExamplePersistentActorRunner extends App {

  val system = ActorSystem("akka-persistence-world")

  val persistentActor = system.actorOf(ExamplePersistentActor.props)

  persistentActor ! Cmd("a")
  persistentActor ! Cmd("b")

  persistentActor ! "snap"
  persistentActor ! "print"

  Thread.sleep(10 * 1000)

  system.shutdown()
}
