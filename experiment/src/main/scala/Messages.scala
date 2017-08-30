package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID
import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.typesafe.config.Config
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}
import spray.json.DefaultJsonProtocol


/* @TODO Figure out a better layout on where these things should go. Especially messages seem to belong right at
 *    the main method / app.
 */


/* Force some of our agents to implement methods to deal with shutting down, reconfiguring or restarting the service.
 *
 */

trait BootstrapShutdown {
  def bootstrap(config:Config):Unit
  def shutdown:Unit
}


/** The typical messages we use between components.
  *
  * TODO: A message containing a list of items? We do send such a message currently, in a response.
  */

object Messages {

  final case class Bootstrap(config: Config)

  final case object Shutdown

  final case object Reboot

  final case class SignalChildren(order:Int) // @TODO: remove once we feel confident we no longer need it for testing

  final case class PrintSignal(order:Int)   // @TODO: remove once we feel confident we no longer need it for testing

  case class PublicKey(
    agentUUID:  UUID,
    publicKey:  String,
    validSince: Long,
    validUntil: Long,
    updatedAt:  Long,
    hash:       String,
    confidence: Int
  )

  case class PublicKeysMessage(
    protocol: Int,
    items:    List[PublicKey]
  )

  case class Get(uuid:UUID)

  case class ChainedGet(uuid:UUID, chain:List[ActorRef])

  case object PublicKey extends SprayJsonSupport with DefaultJsonProtocol {
    implicit object UUIDFormat extends JsonFormat[UUID] {

      def write(uuid: UUID) = JsString(uuid.toString)

      def read(value: JsValue) = {
        value match {
          case JsString(uuid) => UUID.fromString(uuid)
          case _              => throw new DeserializationException("Expected hexadecimal UUID string")
        } }

    }

    // Note the name jsonFormat7. The 7 denotes the number of arguments. For 2 arguments
    //   the function would be jsonFormat2. Here be dragons.
    implicit val PublicKeyFormat = jsonFormat7(PublicKey.apply)
  }

  case object PublicKeysMessage extends SprayJsonSupport with DefaultJsonProtocol {
    // Note the name jsonFormat2. The 2 denotes the number of arguments. Here be dragons.
    implicit val PublicKeysMessageFormat = jsonFormat2(PublicKeysMessage.apply)
  }
}