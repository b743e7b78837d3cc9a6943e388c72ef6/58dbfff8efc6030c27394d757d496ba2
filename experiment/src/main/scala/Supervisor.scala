package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import com.typesafe.config.Config
import akka.actor.{Actor, ActorRef, Props}
import scala.language.postfixOps
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._


/** The Supervisor manages the components that together make the entire system: webclient, datastorage,
  *     fileserver, restservice
  *
  * @param configName
  */

class Supervisor(configName: String) extends Actor {
  import Supervisor._
  import Messages._

  // @TODO: make retries, timerange configureable.
  // @TODO: add specific exceptions for database / file sysetm access / network

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 10 seconds) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Restart // or Escalate
    }

  // @todo: start named services based on config, and map name->actorref
  private var webservers   = scala.collection.mutable.Map[Int,ActorRef]()
  private var restservices = scala.collection.mutable.Map[Int,ActorRef]()
  private var databases    = scala.collection.mutable.Map[Int,ActorRef]()
  private var webClients   = scala.collection.mutable.Map[Int,ActorRef]()

  private var dataSupervisors = scala.collection.mutable.Map[Int,ActorRef]()

  /* Putting the config in a bootstrap message (and having it mutable in the class)
  *     should give us a bit more flexibility in the future when we want to reconfigure
  *     systems on the fly. I'm somewhat torn about this. It's in some ways more
  *     elegant to just restart the actor with a new configuration if we want one, but
  *     I'd like to experiment first to see how well that works out in practice
  */
  def receive = {

    case CreateWebClient(config) => {
      webClients(0) = context.actorOf(Props( new WebClient(config)))
    }

    case CreateDataStorage(config) => {
      databases(0) = context.actorOf(Props( new DatabaseActor(config) ),"Storage")
      databases(0) ! Bootstrap(config)
    }

    case CreateWebServer( config ) =>
      webservers(0) = context.actorOf(Props( new FileService(config) ),"WebServer")
      webservers(0) ! Bootstrap(config)

    case CreateRestService( config ) =>
      dataSupervisors(0) = context.system.actorOf( Props( new DataSupervisor(config,databases(0)) ), "DataSupervisor" )
      restservices(0) = context.actorOf(Props( new PublicKeys( config, dataSupervisors(0), databases(0) )),"RestServer")
      restservices(0) ! Bootstrap(config)

    case Shutdown =>
      context.children foreach( _ ! Shutdown )
  }
}


object Supervisor {
  def props(configName: String): Props = Props(new Supervisor(configName))

  final case class CreateDataStorage(config: Config)
  final case class CreateWebServer  (config: Config)
  final case class CreateRestService(config: Config)
  final case class CreateWebClient  (config: Config)
}
