package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID
import com.typesafe.config.Config
import akka.actor.{Actor, ActorRef, Props}



/** The DataActor takes care of our database needs.
  *
  * @param config
  */

class DatabaseActor(var config:Config) extends PostgresqlDatabase(config) with BootstrapShutdown with Actor{
  import DatabaseActor._
  import Messages._

  def receive() = {
    case GetRequest(view,primaryKeys) =>
      sender ! shortListRequest()

    case Bootstrap(config:Config) =>
      bootstrap(config)

    case Shutdown =>
      shutdown()
  }

  override def preStart(): Unit = { /* @TODO: Shut down db connection */ }


  override def bootstrap(config:Config) = {
    println("Bootstrapping data actor")
    connect()
    createTables()
  }

  override def shutdown() = {}
}


object DatabaseActor {
  def props(config: Config): Props = Props(new DatabaseActor(config))
  final case class GetRequest(view:String, primaryKey:UUID)
  final case class Store(config: Config)
}
