package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.util.UUID

import com.typesafe.config.Config
import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.ListBuffer
import java.sql.Connection

//import name.b743e7b78837d3cc9a6943e388c72ef6.publickeys.Messages.PublicKey
import org.apache.commons.dbcp2._



class PostgresqlDatabase(var dbConfig:Config){
  import Messages.PublicKey

  protected var connectionOption: Option[Connection] = None

  /* Datasource -
   *
   * According to a note in the sourcecode, the poolsize cannot be reset after initialisation
   */

  object Datasource {
    val connectionPool = new BasicDataSource()
    connectionPool.setUsername( dbConfig.getString("postgresql.user") )
    connectionPool.setPassword( dbConfig.getString("postgresql.passphrase") )
    connectionPool.setDriverClassName("org.postgresql.Driver")
    connectionPool.setUrl("jdbc:postgresql://" + dbConfig.getString("postgresql.address") )
    connectionPool.setInitialSize( dbConfig.getInt("postgresql.poolsize") )
  }

  def connect() = {
    connectionOption = Some(Datasource.connectionPool.getConnection)
  }

  def get(item: UUID): Option[PublicKey] = {
    if(!connectionOption.isDefined){ throw new java.sql.SQLException("Not connected to database")}
    var request = s"${dbConfig.getString("queries.get")} '${item.toString}' "
    val results = connectionOption
      .getOrElse(Datasource.connectionPool.getConnection)
      .createStatement()
      .executeQuery( request )

    if(!results.next()) None
    else Some(PublicKey(
      UUID.fromString(results.getString("agent")),
      results.getString("publickey"),
      results.getTimestamp("start").toInstant.getEpochSecond,
      results.getTimestamp("stop").toInstant.getEpochSecond,
      results.getTimestamp("update").toInstant.getEpochSecond,
      results.getString("hash"),
      results.getInt("confidence")
    ))
  }

  /*
 * the select statement isn't a security risk despite not being parameterised / stored procedure, as primaryKey
 *     can only be a UUID.
 */

  def shortListRequest(): List[PublicKey] = {
    if ( ! connectionOption.isDefined )
      throw new java.sql.SQLException("Not connected to database")


    else {
      val request = dbConfig.getString("queries.simpleSelect")

      val results = connectionOption
        .getOrElse(Datasource.connectionPool.getConnection)
        .createStatement()
        .executeQuery( request )

      var objects = new ListBuffer[PublicKey]

      while ( results.next() ) {
        val keyObject = PublicKey(
          UUID.fromString(results.getString("agent")),
          results.getString("publickey"),
          results.getTimestamp("start").toInstant.getEpochSecond,
          results.getTimestamp("stop").toInstant.getEpochSecond,
          results.getTimestamp("update").toInstant.getEpochSecond,
          results.getString("hash"),
          results.getInt("confidence")
        )
        objects += keyObject
      }
      objects.toList
    }
  }

  def createTables():Unit = {
    connectionOption
      .getOrElse(Datasource.connectionPool.getConnection)
      .createStatement()
      .executeUpdate( dbConfig.getString("queries.recreate") )
  }

}