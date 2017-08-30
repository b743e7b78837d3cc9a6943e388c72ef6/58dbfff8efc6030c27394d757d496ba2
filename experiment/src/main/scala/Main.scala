package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import scala.io.StdIn
import akka.actor.PoisonPill
import akka.routing.Broadcast

/* An experimental application that can be used to form a public key encryption distribution (Web of trust) system
 *
 *
 */

object Main extends App {
  import Supervisor._

  implicit val experiment = ActorSystem("experiment")
  implicit val materializer = ActorMaterializer

  try {
    val supervisor = experiment.actorOf(Props( new Supervisor( "application.conf" )), "Supervisor")

    supervisor ! CreateWebClient(   ConfigFactory.load( "webclient.json" ) )
    supervisor ! CreateDataStorage( ConfigFactory.load( "datastorage.conf" ) )
    supervisor ! CreateWebServer(   ConfigFactory.load( "documentation.conf" ) )
    supervisor ! CreateRestService( ConfigFactory.load( "pubkeys.conf" ) )

    println("Press RETURN to shut down the service")
    StdIn.readLine() // let the system run until user presses return

    // Todo: add watch logic in order to shut down the system properly

    supervisor ! Broadcast(PoisonPill)

  } finally {

    experiment.terminate

    println("The actor system has been ordered to shut down")

    System exit(0) // Shutdown.java exit
  }
}
