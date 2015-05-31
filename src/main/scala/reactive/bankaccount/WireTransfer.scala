package reactive.bankaccount

import akka.actor.{Actor, ActorRef}
import reactive.bankaccount.BankAccount.Deposit

class WireTransfer extends Actor {
  import WireTransfer._

  def receive = {
    case Transfer(from, to, amount) =>
      from ! BankAccount.WithDraw(amount)
      context.become(awaitWithDraw(to, amount, sender()))
  }

  def awaitWithDraw(to: ActorRef, amount: BigInt, client: ActorRef): Receive = {
    case BankAccount.Done =>
      to ! Deposit(amount)
      context.become(awaitDeposit(client))
    case BankAccount.Failed =>
      client ! Failed
      context.stop(self)
  }

  def awaitDeposit(client: ActorRef): Receive = {
    case BankAccount.Done =>
      client ! Done
      context.stop(self)
  }
}

object WireTransfer {

  case class Transfer(from: ActorRef, to: ActorRef, amount: BigInt)
  case object Done
  case object Failed
}
