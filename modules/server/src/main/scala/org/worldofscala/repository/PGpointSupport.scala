package org.worldofscala.repository

import com.augustnagro.magnum.*
import org.postgresql.geometric.PGpoint
import org.worldofscala.organisation.LatLon
import java.sql.{PreparedStatement, ResultSet}

trait PGpointSupport {
  val transactor: Transactor

  given DbCodec[LatLon] = new DbCodec[LatLon] {
    override def readSingle(rs: ResultSet, pos: Int): LatLon = {
      val obj = rs.getObject(pos)
      if (rs.wasNull()) {
        LatLon.empty
      } else {
        val point = obj.asInstanceOf[PGpoint]
        LatLon(point.x, point.y)
      }
    }

    override def writeSingle(entity: LatLon, ps: PreparedStatement, pos: Int): Unit =
      ps.setObject(pos, entity, java.sql.Types.OTHER)
  }
}
