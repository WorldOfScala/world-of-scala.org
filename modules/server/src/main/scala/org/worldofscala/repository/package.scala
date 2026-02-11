package org.worldofscala.repository

import com.augustnagro.magnum.DbCodec
import org.postgresql.geometric.PGpoint
import org.worldofscala.organisation.LatLon

import java.sql.PreparedStatement
import java.sql.ResultSet

implicit val lalDbCodec: DbCodec[LatLon] = new DbCodec[LatLon] {

  def cols: IArray[Int]                                                                                       = IArray(java.sql.Types.JAVA_OBJECT)
  def queryRepr: String                                                                                       = "?"
  def readSingleOption(resultSet: java.sql.ResultSet, pos: Int): Option[org.worldofscala.organisation.LatLon] =
    val obj = resultSet.getObject(pos, classOf[PGpoint])
    if (resultSet.wasNull()) {
      None
    } else {
      val point = obj.asInstanceOf[PGpoint]
      Some(LatLon(point.x, point.y))
    }
  override def readSingle(rs: ResultSet, pos: Int): LatLon =
    val obj = rs.getObject(pos, classOf[PGpoint])
    if (rs.wasNull()) {
      LatLon.empty
    } else {
      val point = obj.asInstanceOf[PGpoint]
      LatLon(point.x, point.y)
    }
  override def writeSingle(entity: LatLon, ps: PreparedStatement, pos: Int): Unit =
    ps.setObject(pos, entity, java.sql.Types.OTHER)
}
