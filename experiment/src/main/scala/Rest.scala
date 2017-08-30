package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.model.StatusCodes
import scala.concurrent.Future
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import StatusCodes._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.Config
import scala.util.{Failure, Success}


abstract class Rest {

  type T

  protected var bindingOption:Option[Future[Http.ServerBinding]] = None

  protected var settings = CorsSettings
    .defaultSettings
    .copy(allowGenericHttpRequests = true, allowCredentials = false, allowedOrigins = HttpOriginRange.*)


  protected def getItem(itemId: UUID): Future[Option[T]]

  protected def getUrlPath(): String

  /* This route system can be improved upon by implementing a chain-of-responsibility, where we hand
   *    the request off to the first link in the chain, which can try and handle it, so that if it
   *    succeeds it responds to the original request, and if it fails, hands it off to the next
   *    link in the chain. In this case the first link would be a local hashmap cache, the second link
   *    would depend on if we allow upserts or not - if yes a self-balancing tree, if no a vector -
   *    the third would be a local database, the fourth
   *
   *    See http://www.enterpriseintegrationpatterns.com/patterns/messaging/RoutingTable.html
   *    Currently I suspect this requires creating an actor that can instantiate / supervise
   *    the various chains.
   */

  protected def notAllowed() = complete(StatusCodes.Forbidden)

  protected def notFound() = complete(StatusCodes.NotFound)

  protected def error() = complete(StatusCodes.InternalServerError)

  protected def route: Route = pathEndOrSingleSlash{ notAllowed() }



  implicit def myRejectionHandler =
    RejectionHandler.newBuilder()
      .handle { case MissingCookieRejection(cookieName) =>
        complete(HttpResponse(BadRequest, entity = ""))
      }
      .handle { case MissingQueryParamRejection(param) =>
        complete(HttpResponse(400, entity = ""))
      }
      .handle { case AuthorizationFailedRejection =>
        complete((Forbidden, ""))
      }
      .handle { case ValidationRejection(msg, _) =>
        complete((InternalServerError, "" + msg))
      }
      .handleAll[MethodRejection] { methodRejections =>
      val names = methodRejections.map(_.supported.name)
      complete((MethodNotAllowed, s"Supported: ${names mkString " or "}"))
    }
      .handleNotFound {
        complete((400, ""))
      }
      .result()


  protected def bind(config: Config) = {
    import akka.stream._
    // Bug 261660fa-10ee-45e8-a62e-7004b7565536
    // There was a problem with the route in bindandhandle not converting to a routeFlow. Using the following commented arguments
    //    the problem could be solved, but as we're overriding bind anyway, it seems better to just put a new actorsystem
    //    and materialiser in here. I leave this comment in here, in case the system gets modified in the future.
    // import akka.stream.scaladsl._
    // import akka.NotUsed
    // val routeFlow: Flow[HttpRequest, HttpResponse, NotUsed] = RouteResult.route2HandlerFlow(route)
    // val bindingFuture = Http()(system)
    //    .bindAndHandle(routeFlow, config.getString("http.host"), config.getInt("http.port"))(materializer)
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val system = ActorSystem("REST")
    implicit val materializer = ActorMaterializer()

    val bindingFuture = Http()(system).bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))(materializer)

    bindingFuture.onComplete {
      case Success(serverBinding) => bindingOption = Some(bindingFuture)
      case Failure(ex) => throw new java.net.BindException("bindAndHandle failed. Presumably the port is already in use.")
    }
  }

}
