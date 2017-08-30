package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.pattern.pipe
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import scala.util.{Failure, Success}


/** The WebClient is a minimal experiment to see how to issue GET requests in akka. It works, but needs work
  *     in order to be useful.
  *
  * Consider the following Aaka dcumentation comment:
  *   Be sure to consume the response entities dataBytes:Source[ByteString,Unit] by for example connecting it to a Sink
  *   (for example response.discardEntityBytes() if you don’t care about the response entity), since otherwise Akka
  *   HTTP (and the underlying Streams infrastructure) will understand the lack of entity consumption as a
  *   back-pressure signal and stop reading from the underlying TCP connection!
  *
  * Note that the minimal implementation below is a security risk, as any request for
  *    an unknown agent gets multiplied by the number of known other keyservers (currently 1,
  *    but potentially many). This can lead to a storm of requests, and
  *    is not intended to take into production as is. The most obvious
  *    improvement is to group requests for unknown agents, and for those whose confidence
  *    needs to be increased, and batch process them. A secondary improvement is to only allow such
  *    requests from known agents (those who themselves have their key stored within the system).
  *
  * Note: http://doc.akka.io/docs/akka-http/current/scala/http/client-side/request-level.html
  *    you should not access the Actors state from within the Future’s callbacks (such as map, onComplete, …) and
  *    instead you should use the pipeTo pattern to pipe the result back to the Actor as a message.
  *
  */

class WebClient(var config:Config) extends Actor with BootstrapShutdown {
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  import context.dispatcher

  protected val http = Http(context.system)
  protected var bindingOption:Option[Future[Http.ServerBinding]] = None
  protected val serverAddresses = config.getStringList("keyservers")


  override def bootstrap(config:Config) = {}

  override def shutdown() = {}


  def receive = {

    case "test" => {
      val responseFuture: Future[HttpResponse] = Http(context.system)
        .singleRequest(HttpRequest(uri = "http://example.com"))

      responseFuture onComplete {

        case Success(response) =>
          println( "Request at example.com completed succesfully:\n\n" + response)

        case Failure(e) =>
          println("Error: " + e.getMessage)
      }
    }

    case agent:UUID => {
      println( "Request for " + agent.toString)

      serverAddresses.forEach(
        address => {
          /* Looking at the source of HttpRequest it seems we construct a get request and should put the
           *    parameters in the uri
           */
          http.singleRequest(HttpRequest(uri = s"${address}?${agent.toString}"))
            .pipeTo(self)
        }
      )
    }

    case resp @ HttpResponse(StatusCodes.OK, headers, entity, _) => {
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach {
        body => {
          val response = body.utf8String
          println(response)
        }
      }
      resp.discardEntityBytes()
    }

    case resp @ HttpResponse(code, _, _, _) => {
      println("HTTP Response - Code: " + code)
      resp.discardEntityBytes()
    }
  }
}



object WebClient {
  def props(config: Config): Props = Props(new WebClient(config))
}
