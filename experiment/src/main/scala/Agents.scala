package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import java.time.Instant
import java.util.UUID
import collection.mutable.HashMap
import com.typesafe.config.ConfigFactory

/** Agents are hardcoded entities used to test the system. Their ultimate purpose is to be removed
  *     from the codebase as it matures.
  *
  */

object Agents {
  import Messages._

  private val keys = ConfigFactory.load( "hardcodedKeys.conf" )
  private val validSince: Long = Instant.EPOCH.getEpochSecond
  private val unixTimestamp: Long = Instant.now.getEpochSecond
  private val validUntil : Long = Instant.MAX.getEpochSecond

  val uuidA = UUID.fromString( "38400000-8cf0-11bd-b23e-10b96e4ef00d" )
  val uuidB = UUID.fromString( "38400000-8cf0-11bd-b23e-10b96e4ef00e" )
  val uuidC = UUID.fromString( "38400000-8cf0-11bd-b23e-10b96e4ef00f" )
  val uuidZ = UUID.fromString( "38402000-8cf0-11bd-b23e-10b96e4ef00e" )

  /* temporary hardcoded data, intended for testing. Map format is ( ID -> ( publicKey, timestamp,timestamp,timestamp.confidence) ) */
  private val agents = HashMap(
    uuidA -> PublicKey( uuidA, keys.getString("agents.publicA"), validSince, validUntil, unixTimestamp, "somhash0snmh", 100 ),
    uuidB -> PublicKey( uuidB, keys.getString("agents.publicB"), validSince, validUntil, unixTimestamp, "ladidahash", 100 ),
    uuidC -> PublicKey( uuidC, keys.getString("agents.publicC"), validSince, validUntil, unixTimestamp, "hash9sefdesash", 100 ),
    uuidZ -> PublicKey( uuidZ, keys.getString("agents.publicC"), validSince, validUntil, unixTimestamp, "asdfshash", 22 )
  )

  def data = agents
  def get(id:UUID) = agents.get(id)
}
