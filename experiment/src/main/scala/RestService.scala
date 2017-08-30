package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import akka.actor.{Actor, ActorRef}
import com.typesafe.config.Config
import akka.util.Timeout
import scala.concurrent.duration._


abstract class RestService(dataActor: ActorRef) extends Rest with BootstrapShutdown with Actor {
  import Messages._
  import context.dispatcher

  implicit val timeout = Timeout.apply(2,SECONDS)


  def receive = {
    case Bootstrap(config) =>
      println("REST service bootstrapping");
      bootstrap(config)
    case Shutdown =>
      println("REST service shut down stub")
    case Reboot =>
      println("REST service rebooting stub");
  }


  override def bootstrap(config:Config) = {
    bindingOption match {
      case None =>
        bind(config)

      case Some(bindingFuture) =>
        bindingFuture.flatMap(_.unbind())
        bind(config)
    }
  }


  override def shutdown() = {
    import context.dispatcher
    bindingOption match {
      case None =>
      case Some(bindingFuture) => bindingFuture.flatMap(_.unbind())
    }
  }


}
