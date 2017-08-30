package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.net.BindException
import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import com.typesafe.config.Config
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import scala.language.reflectiveCalls
import scala.util.{Failure, Success}


/** Fileserver - intended to expose a limited number and restricted range of files to the outside world.
  *
  * Currently the code is written to only expose html, js, css, and svg. This is hardcoded as I will have
  *   to spend a bit more time looking into how exactly responses to queries are constructed.
  *
  * The fileserver is built to only expose files within a specific directory, and not rely on user input, in
  *   order to prevent filesystem traversal attacks. To expand the system beyond the single directory structure
  *   makes sense, for the future.
  *
  */


class FileService(config:Config) extends FileServer(config) with Actor with BootstrapShutdown {
  import Messages._
  import context.dispatcher

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))


  def receive = {

    case PrintSignal(n) =>
      println(s"Signal received - order $n")

    case Bootstrap(config) =>
      println("Bootstrapping file server")
      bootstrap(config)

    case Shutdown =>
      println("File server shut down is only a stub!")

    case Reboot =>
      println("File server reboot is only a stub!");
    // @TODO: Once an orchestrator is built, use it to redirect clients, finish message queue, send poisonpill to self.
  }


  //@TODO SSL: this seems to be part of akka (there's a DefaultSSLContextCreation in the source),
  //    so there should be docs on it.
  override def bootstrap(config:Config) = {

    bindingOption match {
      case Some(bindingFuture) =>
        throw new BindException("Port already bound")

      case None => {
        loadedFiles = loadFiles( config )
        val bindingFuture = Http()(context.system)
          .bindAndHandle(
            makeRoute(config),
            config.getString("http.host"),
            config.getInt("http.port")
          )

        bindingFuture.onComplete {
          case Success(serverBinding) =>
            bindingOption = Some(bindingFuture)

          case Failure(ex) =>
            throw new BindException()
        }
      }
    }
  }


  override def shutdown() = bindingOption match {

      case None =>

      case Some(bindingFuture) =>
        bindingFuture.flatMap(_.unbind())
  }
}



/**
  *
  */

object FileService {
  def props(config: Config): Props = Props(new FileService(config))
}
