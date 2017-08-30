package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys
// Feature 8b53a35a-47ee-4c56-8ec1-e0c638dc29c5

import akka.actor.{Actor, ActorRef, Props, Terminated}

object ShutdownSystem {
  def props: Props = Props[ShutdownSystem]
}

class ShutdownSystem( supervisor:ActorRef ) extends Actor {
  import Main._
  context.watch(supervisor)

  def receive = {
    case Terminated(actor) if( actor == supervisor ) =>
        println("System termination stub")

    case _ =>
      println("Unknown message")
  }
}
