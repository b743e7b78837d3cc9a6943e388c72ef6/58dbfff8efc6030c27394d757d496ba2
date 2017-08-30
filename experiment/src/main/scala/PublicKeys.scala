package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID
import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.Config
import akka.pattern.ask
import scala.concurrent.Future


/* a PublicKey is used to store and communicate the public keys and associated metadata.
 *    Once a DB storage mechanism is fully implemented this class should work with the hash
 *    that is computed from the relevant fields (agentUUID, publicKey, validSince, validUntil,
 *    confidence) which together with the updatedAt field allows concurrent use by guarding against race
 *    conditions. Currently we store the data in postgresql, so we can implement atomic transactions,
 *    but there's a slight possibility we wish to change this later. Specifically if we want
 *    to move to a distributed database system atomicity cannot be guaranteed.
 */


/** PublicKeyRestServiceActor creates a minimal rest service where one may request public PGP keys.
  *     Once a DB storage mechanism is fully implemented the service should be improved upon by
  *
  *     A) including POST and PUT/PATCH methods
  *     B) using the validSince and validUntil values to decide which key to respond with
  *     C) potentially including a hash mechanism here instead of inside the database
  */

class PublicKeys(config:Config, dataSupervisor: ActorRef, dataActor: ActorRef)
  extends RestService( dataActor )
    with PublicKeysOnRead {

  import scala.concurrent.ExecutionContext.Implicits.global
  import DatabaseActor._
  import Messages._
  type T = PublicKey

  /**
    * First try and get the thing we're looking for from the (currently hardcoded) agents whose information
    *   is already inside the application. But if that doesn't work, find it in the database instead. If its
    *   not in there find it somewhere else on the web, once multiple servers are online/configured.
    *
    * @param itemId
    * @return
    */
  override protected def getItem(itemId: UUID): Future[Option[PublicKey]] = Future {
    Agents.data.get(itemId) match {
      case None => {
        dataActor ! GetRequest( "agents", itemId )
        None
      }

      case Some(publicKey) => {
        Some(publicKey)
      }
    }
  }

  override protected def getUrlPath() = {
    config.getString("http.path")
  }

  // @TODO: use pattern to refactor routes
  // IntelliJ is occasionally confused about these parameters, showing a cannot resolve reference error,
  //     but everything compiles and runs fine...
  protected val requestGet = path( getUrlPath() / JavaUUID ) &
    get &
    parameters( 'sessionLogId.as[String], 'protocol.as[Int] )

  /* @TODO: refactor for multiple routes
  *  @TODO: Consider a cleaner way for actor-per-request pattern.
  *     See also https://markatta.com/codemonkey/blog/2016/08/03/actor-per-request-with-akka-http/
  * */
override protected def route: Route = get {
  path(getUrlPath() / JavaUUID) {
    (itemId) => {
        parameters( 'sessionLogId.as[String], 'protocol.as[Int] ) {
          (sessionLogId, protocolVersion) => {
            onSuccess((dataSupervisor ? Get(itemId)).mapTo[PublicKeysMessage]){
              result => cors(){
                if (result.items.isEmpty)
                  notFound()
                else {
                  val message = new PublicKeysMessage(1, transform(result.items))
                  complete(message)
                }
              }
            }
          }
        }
      }
    }
  }
}


object PublicKeys {
  def props(config: Config, dataSupervisor: ActorRef, dataActor: ActorRef): Props = Props(new PublicKeys(config, dataSupervisor, dataActor))
}


/*
 * Only one of the following traits is supposed to be mixed in, depending on whether the application is read-heavy or
 *    write-heavy. The lack of an override modifier on the transform method means that the compiler accepts only one
 *    trait being mixed in, which is exactly what we intend - for now.
 *
 * We may consider expanding this to a full Model-View-Controller architecture, if we ever expand this application.
 *
 * On modifying, take into consideration PublicKeysOnWrite
 */

trait PublicKeysOnRead {
  import Messages.PublicKey
  def transform(items:List[PublicKey]) = {
    items
  }
}

/*
 * See the comment on PublicKeysONRead
 */

trait PublicKeysOnWrite {
  import Messages.PublicKey
  def transform(items:List[PublicKey]) = {
    items
  }
}