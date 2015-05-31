package reactive.bankaccount

import akka.actor.Actor

class BankAccount extends Actor {

  import BankAccount._

  var balance = BigInt(0)

  def receive = {
    case Deposit(amount) =>
      balance += amount
      sender ! Done
    case WithDraw(amount) if amount <= balance =>
      balance -= amount
      sender ! Done
    case _ =>
      sender ! Failed
  }
}

object BankAccount {
  case class Deposit(amount: BigInt) {
    require(amount > 0)
  }
  case class WithDraw(amount: BigInt) {
    require(amount > 0)
  }

  case object Done
  case object Failed
}
