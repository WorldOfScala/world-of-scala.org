package org.worldofscala.earth

import org.worldofscala.BaseEndpoint
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import sttp.tapir.json.zio.*
import zio.stream.ZStream

import java.io.InputStream

object MeshEndpoint extends BaseEndpoint {

  val streamCreate: Endpoint[String, (String, InputStream), Throwable, Mesh.Id, Any] = baseSecuredEndpoint
    .tag("Admin")
    .name("mesh-create")
    .post
    .in("mesh" / path[String]("name"))
    .in(
      inputStreamBody
        .description("Mesh to insert")
    )
    .out(jsonBody[Mesh.Id])
    .description("Create mesh")

  val putThumbnail: Endpoint[String, (Mesh.Id, InputStream), Throwable, Mesh.Id, Any] = baseSecuredEndpoint
    .tag("Admin")
    .name("mesh-create")
    .put
    .in("mesh" / path[Mesh.Id]("id") / "thumbnail")
    .in(inputStreamBody.description("Thumbnail"))
    .out(jsonBody[Mesh.Id])
    .description("Create mesh")

  val all: PublicEndpoint[Unit, Throwable, Seq[MeshEntry], Any] = baseEndpoint
    .tag("Admin")
    .name("meshes")
    .get
    .in("mesh")
    .out(jsonBody[Seq[MeshEntry]])
    .description("Get all meshes")

  val get: Endpoint[Unit, Mesh.Id, Throwable, ZStream[Any, Throwable, Byte], ZioStreams] = baseEndpoint
    .tag("Admin")
    .name("mesh")
    .get
    .in("mesh" / path[Mesh.Id]("id"))
    .out(streamBody(ZioStreams)(Schema.binary, CodecFormat.OctetStream()))
    .description("Get mesh")

//   val allStream: Endpoint[Unit, Unit, Throwable, Stream[Throwable, Byte], ZioStreams] = baseEndpoint
//     .tag("Admin")
//     .name("mesh stream")
//     .get
//     .in("mesh" / "stream")
//     .out(streamBody(ZioStreams)(Schema.derived[(Mesh.Id, String)], CodecFormat.TextEventStream()))
//     .description("Get all meshes")

}
