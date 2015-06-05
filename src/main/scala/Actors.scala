/* Simple actor repeating what you send to it */

import scala.actors._

class Echo(times: Int) extends Actor {
  def act {
    while (true) {
      receive {
        case s: String => repeatString(s)
        case i: Int => repeatInt(i)
        case x => println(s"Dunno what that is: $x.")
      }
    }
  }
  def repeatString(s: String) {
    /* Let's just print the string! */
    for (i <- 0 until times) println(s)
  }
  def repeatInt(i: Int) {
    /* Let's repeat the int, but say it's an int as well */
    for (i <- 0 until times) println(s"Int received: $i.")
  }
}