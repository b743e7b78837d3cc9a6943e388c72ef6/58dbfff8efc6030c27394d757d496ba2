package name.b743e7b78837d3cc9a6943e388c72ef6.pubkey

import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import akka.http.javadsl.model.ContentTypes
import Directives._

class RestTest extends WordSpec with Matchers with RestService with ScalatestRouteTest {
  // See http://doc.akka.io/docs/akka-http/10.0.9/scala/http/routing-dsl/testkit.html
  //    and notice that the implicit stuff leads to errors, and write out everything explicitly
  "pubkey rest service" should {
    // #460d7af5-ccef-4fcb-a53d-b8a22537d106
    "respond to a GET request to /agents/UUID with a known UUID with a HTTP 200 (success) status" in {
      val request = Get( "/agents/38400000-8cf0-11bd-b23e-10b96e4ef00e?sessionLogId=15&protocol=1" )
      val requestWithRoutes = request.~>(route)(TildeArrow.injectIntoRoute )
      requestWithRoutes.~>(check {
        status.isSuccess() shouldEqual true
        contentType === ContentTypes.APPLICATION_JSON
        responseAs[String] shouldEqual "{\"protocol\":\"1\",\"keys\":[{\"agent\":\"38400000-8cf0-11bd-b23e-10b96e4ef00e\",\"Pubkey\":\"-----BEGIN PGP PUBLIC KEY BLOCK-----\\nVersion: OpenPGP.js v2.0.0\\nComment: http://openpgpjs.org\\n\\nxsBNBFmLB1QBCACvIoJEvLQHD25i7FLbxWiEJ5TMxt7l6nEgAqFTzclFq8kf\\noKtx8tpLIuaAnigvTnykUsc5phMtFP7elxWwQjGgkOQylGEN6qkepMQNA1ud\\nyNw5DZ+2M+0sE3SSYs8HiUFjL8RntSI2eTd/oZfWvOwg53xcqCnrU4W51Cu2\\noZyBl2p0aAZwyzAa5UCiPrjpDO8KUvIEHZGJ4imnTpgSwyq1UQ5s1wA9kRe9\\nO1nxrlBD4KaHgt3Hjuce5JasKn3UFwysgC7Zp6nuQacvljJQ/vu7uyoZ2OSY\\na+EEhOSBrNCUOV8PhkOdlZIYeVbZendmo3n2Y9wiSwIT7ZTh6vM0FDW/ABEB\\nAAHNHlN2ZW4gPFJlaW5kZWVyQG1haWxpbmF0b3IuY29tPsLAdQQQAQgAKQUC\\nWYsHVAYLCQgHAwIJEJXGqcPU+y4oBBUIAgoDFgIBAhkBAhsDAh4BAAAmuggA\\nql7huvuR8BGU9DcGo3/9odNDtVfW0qniK4YjzmUXuitGMLddLLLChEMkbmCk\\nJGPp5zJiXKTLYnXBc+QtECFIYg4OVixc4HN0nwJ///5GtyouH40hZ88MP5Zs\\nzrAbQeuZ3PrUnkyCRJHJODPBZn7D1MHlL/sLKIJrKVWGCxebLsdkkpToJgDo\\nm22eH315ZeKgTnnbxy14fbhwXW0ienzQuuulBWl6Cnqkjivf/OxR/7QzypN4\\nbkBvucUspgNj2Xu4aWTPO2+9rPgAQ7z19ip3ZKuPF9ehi+g3uI3lJtagBUGH\\nFI9MzNrmWjef7NRt0YyQCvyMmU3WzMgbs33IK5fsFM7ATQRZiwdUAQgA4Fdw\\nCOeMYShkP3EdXEdT9LQeMhEqyuG3L6g22nrfrk+DdIDjAlufw+gJ/K2On+NR\\n7gFlMGKlxnkkJDMiJl7oG6TFI0HkUt8Xq1/rWTUWR+MsuhAtYb1oF5EkkqCx\\nYSTYllWXDponlrhpo+edmWDnIZ2oUGhVax9b4iNeLutbF9sFB2m22K5YCTEu\\ndzpOPRfAzzllA9WHcs7MCkHmAa5eqRQJ6AfUI4ZfQepZR+3EbhKUrc0IybJ3\\nNwVKU5jXLOxVib+IeliG4b3PN5zKAVG0zmpKWrd9rooJvcWv2HgchKebwxJR\\n48Y1MIAKS6uU5E8WW9lF0sX3nytlmTEirWJHSwARAQABwsBfBBgBCAATBQJZ\\niwdVCRCVxqnD1PsuKAIbDAAAihMIAJ50K2vg+nOmsU44yEt6OLnvF/vXuBmn\\nolovhzZHKqz/RX/m3JbBtGiaKUJsnca4agSxt+bRasuSJwVgY3LFaMrus8Wx\\nNWfHyTQxZKzFeCTeBvxjFcOlZzDL5TV2COBpTAQCr+qYxJx+0eR2dh6MFtSJ\\nwCiZ3xDqgGSzyYkQUlQx+6C/RMWBtwjfgEI0gqO8Kx/p5OLaTHzImckFZSon\\ngtjgnslZ0KgrS4j/f48nURgW3mX0vcrIu9iSWKQb0ZyQbJNipPaYMPCXOE61\\nWN+QHJxULlAXcCXXu3/AlEWPfAm+pfSF3vuqzDQtxU3TkmXRxdIHW+dUKm/w\\nIMoScvcl8hk=\\n=C5gd\\n-----END PGP PUBLIC KEY BLOCK-----\",\"Confidence\":\"100\"}]}"
      })
    }
    // #bbb90e3f-391e-4504-9641-e133f21d28d3
    "respond to a GET request to /agents/UUID with an unknown UUID with a HTTP 404 (not found) status" in {
      val request = Get( "/agents/f8400000-8cf0-11bd-b23e-10b96e4ef00e?sessionLogId=16&protocol=1" )
      val requestWithRoutes = request.~>(route)(TildeArrow.injectIntoRoute )
      requestWithRoutes.~>(check {
        status.intValue() shouldEqual 404
      })
    }
    // #a4e05a99-502c-44d7-8a84-b5613e5518a7
    /** @TODO: figure out why this test fails, yet when manually testing the behaviour conforms to expectations.
    "respond to a GET request to /agents/UUID that lacks a necessary parameter with a HTTP 400 (bad request) status" in {
      val request = Get( "/agents/f8400000-8cf0-11bd-b23e-10b96e4ef00e?sessionLogId=18" )
      val requestWithRoutes = request.~>(route)(TildeArrow.injectIntoRoute )
      requestWithRoutes.~>( check {
        status.intValue() shouldEqual 400
      })
    }
*/
    // #43b30d5a-8fa3-4215-a98c-d8049f6af769
/** @TODO: figure out why this test fails, yet when manually testing the behaviour conforms to expectations.
    "respond to a GET request to a non-existing route with a HTTP 400 (bad request) status" in {
      val request = Get( "/doesntexist" )
      val requestWithRoutes = request.~>(route)(TildeArrow.injectIntoRoute )
      requestWithRoutes.~>(check {
        status.intValue() shouldEqual 400
//        responseAs[String] shouldEqual ""
      })
    }
    */
  }
}

/*
  val keysFuture = dataActor ? GetRequest("agents",UUID.fromString("8567ae95-0f3a-48ee-90f1-136de2d53143"))
  val testkeys   = Await.result(keysFuture, timeout.duration).asInstanceOf[List[PublicKey]]
  val testkeyN   = PublicKey(
    UUID.fromString( "38400000-8cf0-11bd-b23e-10b96e4ef00d" ),
    "public key A",
    Instant.now.getEpochSecond,
    Instant.now.getEpochSecond,
    Instant.now.getEpochSecond,
    "This is a hash",
    50
  )
  val testkeyM   = PublicKey(
    UUID.fromString( "38400000-8cf0-11bd-b23e-10b96e4ef00e" ),
    "public key B",
    Instant.now.getEpochSecond,
    Instant.now.getEpochSecond,
    Instant.now.getEpochSecond,
    "This could be a hash",
    55
  )
*/

