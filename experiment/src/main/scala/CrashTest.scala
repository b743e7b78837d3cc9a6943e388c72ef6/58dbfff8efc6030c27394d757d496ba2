package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID

import com.typesafe.config.Config
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.{Actor, ActorSystem, Props}

// import org.apache.commons.dbcp2.{PoolingDataSource, DelegatingConnection}
// import org.postgresql.{PGNotification, PGConnection}
// import scalikejdbc._
// import org.json4s.native.JsonMethods._
// import org.json4s.DefaultFormats
// import scala.concurrent.duration._
import java.sql.Connection
import java.sql.Statement
import org.apache.commons.dbcp2._

object CrashTest {
  def props(m: Int): Props = Props(new CrashTest(0))
  def props: Props = Props[CrashTest]
  final case class GetRequest(view:String, primaryKey:UUID)
  final case class Store(config: Config)
}

class CrashTest(m:Int = 0) extends Actor {
  import CrashTest._
  import Messages._

  var n = m
  println("Crashtest Constructor n = " + n )

  override def preStart(): Unit = {
    n += 1
    println("Crashtest preStart n = " + n )
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println(s"\n\nCrashtset preRestart n = $n \n\n")
  }

  override def postRestart(reason: Throwable): Unit =
    println(s"Crastest postRestart n = $n")

  override def postStop(): Unit =
    println("Crashtest postStop")


  def receive() = {
    case "crash" => throw new Exception("Forcing restart")
    case "test" => println("test")
  }

}
