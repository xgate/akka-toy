package reactive.bankaccount

class BankAccount {

  private var balance = 0

  def deposit(amount: Int): Unit = this.synchronized {
    if (amount > 0) balance = balance + amount
  }

  def withDraw(amount: Int): Int = this.synchronized {
    val b = balance
    if (0 < amount && amount <= b) {
      val newBalance = b - amount
      balance = newBalance
      newBalance
    } else {
      throw new Error("insufficient funds")
    }
  }

  def transfer(from: BankAccount, to: BankAccount, amount: Int): Unit = {
    from.synchronized {
      to.synchronized {
        from.withDraw(amount)
        to.deposit(amount)
      }
    }
  }
}
