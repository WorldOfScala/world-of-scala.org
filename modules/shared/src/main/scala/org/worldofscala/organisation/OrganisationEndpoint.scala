package org.worldofscala.organisation

import org.worldofscala.BaseEndpoint

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import zio.*
import zio.stream.*
import org.worldofscala.user.UserView
import org.worldofscala.domain.organisation.Mesh

object OrganisationEndpoint extends BaseEndpoint:

  import UserView.given
  import OrganisationView.given

  val create: Endpoint[String, NewOrganisationWiew, Throwable, OrganisationView, Any] = baseSecuredEndpoint
    .tag("Organisation")
    .name("organisation")
    .post
    .in("organisation")
    .in(
      jsonBody[NewOrganisationWiew]
        .description("Organisation to insert")
        .example(
          NewOrganisationWiew(
            "EPFL",
            LatLonView(46.519653, 6.632273),
            Mesh.default
          )
        )
    )
    .out(jsonBody[OrganisationView])
    .description("Create organisation")

  val all: PublicEndpoint[Unit, Throwable, Seq[OrganisationView], Any] = baseEndpoint
    .tag("Organisation")
    .name("organisation")
    .get
    .in("organisation")
    .out(jsonBody[Seq[OrganisationView]])
    .description("Get all organisations")

  val allStream: Endpoint[Unit, Unit, Throwable, Stream[Throwable, Byte], ZioStreams] = baseEndpoint
    .tag("Organisation")
    .name("Organisations stream")
    .get
    .in("organisation" / "stream")
    .out(streamBody(ZioStreams)(Schema.derived[OrganisationView], CodecFormat.TextEventStream()))
    .description("Get all organisations")
