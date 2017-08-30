package name.b743e7b78837d3cc9a6943e388c72ef6.publickeys

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.Config
import scala.concurrent.Future
import scala.language.reflectiveCalls


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

class FileServer(config:Config) {

  protected var bindingOption:Option[Future[Http.ServerBinding]] = None

  protected var loadedFiles = loadFiles( config )

  def makeRoute( config:Config ) = get {
    pathEndOrSingleSlash {
      cors()
      getFromResource( s"${config.getString("files.basedir")}/${config.getString("files.baseFile")}" )
    } ~ path( loadFiles(config) ) {
      (mappedContent) => {
        // todo: map to path instead of an httpentity, construct response, though for a miniature system
        //     such as this, it's hardly relevant
        complete(StatusCodes.OK -> mappedContent)
      }
    }
  }

  def loadFiles(config:Config): Map[String, HttpEntity.Strict ] =
    new java.io.File("./src/main/resources/documentation") //@TODO: look into NIO, resourcepath. Make path configureable
      .listFiles
      .filter( file =>
        file.isFile && validName( file.getName ) // Or look at mimetype
      ).map { path =>
      using( scala.io.Source.fromFile( path, "utf-8")) {
        source => {
          (
            path.getName,
            filenameToEntity( getExtensions(path.getName), source.mkString )
            ) } }
    }.toMap


  // We want to know and limit what we open up to the world.
  // @todo: add a function to look at mimetype

  def filenameToEntity( extension: String, content: String ): HttpEntity.Strict = extension match {
    case "html" => HttpEntity( ContentTypes.`text/html(UTF-8)`, content )
    case "css"  => HttpEntity( ContentType(MediaTypes.`text/css`,HttpCharsets.`UTF-8`), content )
    case "js"   => HttpEntity( ContentType(MediaTypes.`application/javascript`,HttpCharsets.`UTF-8`), content )
    case "svg"  => HttpEntity( MediaTypes.`image/svg+xml`, content.getBytes("utf-8"))
  }


  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B = try {
    f(resource)
  } finally {
    resource.close()
  }


  def validName(filename:String): Boolean = {
    if (
      (filename endsWith ".html") ||
        (filename endsWith ".svg")  ||
        (filename endsWith ".css")  ||
        (filename endsWith ".js")
    ) true
    else false
  }


  def getExtensions(name: String) = {
    val regex = """^.*\.(\w+)$""".r
    val regex(ext) = name
    ext
  }
}

