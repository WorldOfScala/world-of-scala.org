package org.worldofscala.organisation

import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import org.worldofscala.BaseEndpoint
import sttp.capabilities.zio.ZioStreams

import zio.stream.*
import org.worldofscala.earth.Mesh
object OrganisationEndpoint extends BaseEndpoint:

  val create: Endpoint[String, NewOrganisation, Throwable, Organisation, Any] = baseSecuredEndpoint
    .tag("Organisation")
    .name("organisation")
    .post
    .in("organisation")
    .in(
      jsonBody[NewOrganisation]
        .description("Organisation to insert")
        .example(
          NewOrganisation(
            "EPFL",
            LatLon(46.519653, 6.632273),
            Mesh.default
          )
        )
    )
    .out(jsonBody[Organisation])
    .description("Create organisation")

  val all: PublicEndpoint[Unit, Throwable, Seq[Organisation], Any] = baseEndpoint
    .tag("Organisation")
    .name("organisation")
    .get
    .in("organisation")
    .out(jsonBody[Seq[Organisation]])
    .description("Get all organisations")

  val allStream: Endpoint[Unit, Unit, Throwable, Stream[Throwable, Byte], ZioStreams] = baseEndpoint
    .tag("Organisation")
    .name("Organisations stream")
    .get
    .in("organisation" / "stream")
    .out(streamBody(ZioStreams)(Schema.derived[Organisation], CodecFormat.TextEventStream()))
    .description("Get all organisations")
