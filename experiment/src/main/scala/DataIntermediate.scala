package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID
import Messages._
import akka.pattern.ask
import scala.util.{Success, Failure}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent._

import com.typesafe.config.{Config, ConfigFactory}
import akka.actor.{Actor, ActorRef, Props}


/* Note the extreme form of code repetition here. This was only an experiment to see if
 *    some chain-of-responsibility could be implemented. This will need refactoring
 *    before actually using it.
 */

/** The DataSupervisor takes care of our database needs.
  *
  * @param config
  */



class DataSupervisor(config: Config, db: ActorRef) extends Actor {
  val postgresDB = new PostgresqlDatabase(config)
  postgresDB.connect()
  val chainedCache = context.actorOf(Props( new DatachainCache() ),"Storage")
  val chainedDB = context.actorOf(Props( new DatachainRelationalDB(postgresDB)))

  val chain = List(chainedCache, chainedDB)
  var boss:Option[ActorRef] = None


  def receive() = {
    case Get(itemId) => {
      println("Supervisor getting " + itemId)
      boss = Some(sender())
      implicit val askTimeout: Timeout = 3.seconds
      chainedCache forward ChainedGet(itemId,chain)
    }

    case PublicKeysMessage(1,_) => {
      println("Supervisor receiving message with item")
    }

    case _ =>
      println("Unexpected request to data supervisor")
  }
}

object DataSupervisor {
  def props(config: Config, db:ActorRef): Props = Props(new DataSupervisor(config,db))
}



class DatachainCache() extends Actor {
  def receive() = {
    case PublicKey =>
      println("DatachainCache: Public key stub")

    case ChainedGet(itemId, chain) =>
      println("Cache getting " + itemId)
      Agents.get(itemId) match {
        case None =>
          println("Cache didnt find item.")
          if (!chain.tail.isEmpty) {
            println("Next in chain found. Forwarding")
            chain.tail.head forward ChainedGet(itemId, chain.tail)
          }
          else {
            println("Next in chain not found. Returning empty message")
            sender() ! PublicKeysMessage(1,List())
          }

        case Some(item:PublicKey) =>
          println("Cache sending PublicKey")
          sender() ! PublicKeysMessage(1,List(item))
      }

    case _ =>
      println("Unexpected message for DatachainCache ")
  }
}


object DatachainCache {
  def props(): Props = Props(new DatachainCache())
}



class DatachainRelationalDB(database:PostgresqlDatabase) extends Actor{

  def receive() = {

    case ChainedGet(itemId, chain) => {
      println("Database getting " + itemId)
      database.get(itemId) match {
        case Some(item: PublicKey) =>
          println("Database sending publickey")
          sender() ! PublicKeysMessage(1, List(item))

        case None =>
          if (!chain.tail.isEmpty) {
            chain.tail.head forward Get(itemId)
          } else {
            println("Database didn't find item, sending empty list")
            sender() ! PublicKeysMessage(1, List())
          }
      }
    }

    case _ =>
      println("Unexpected message for DatachainRelationalDB")
  }
}

object DatachainRelationalDB {
  def props(database:PostgresqlDatabase): Props = Props(new DatachainRelationalDB(database))
}
