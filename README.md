# Obey-migration-example

## Overivew

This is an example of migration rule that takes an actor from `scala.Actors` and transforms it into a `akka.actor.Actor`. Note that `scala.Actors` are deprecated.

For this rule to work, please checkout the `withInferencer` version of Obey at: XXX.

## Example

This rule is able to transform actors such as:
```scala
import scala.actors._

class Echo(times: Int) extends Actor {
  def act {
    while (true) {
      receive {
        case x => doSomething
      }
    }
  }
}


```
...into:
```scala
import akka.actors._

class Echo(times: Int) extends Actor {
  def receive {
    case s: String => repeatString(s)
    case i: Int => repeatInt(i)
    case x => println(s"Dunno what that is: $x.")
  }
}
```
...while keeping layout and comments.