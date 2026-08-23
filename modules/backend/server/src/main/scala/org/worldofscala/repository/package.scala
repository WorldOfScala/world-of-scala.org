package org.worldofscala.repository

import com.augustnagro.magnum.DbCodec
import org.postgresql.geometric.PGpoint
import org.worldofscala.organisation.LatLonView

import java.sql.PreparedStatement
import java.sql.ResultSet

implicit val lalDbCodec: DbCodec[LatLonView] = new DbCodec[LatLonView] {

  def cols: IArray[Int]                                                                                           = IArray(java.sql.Types.JAVA_OBJECT)
  def queryRepr: String                                                                                           = "?"
  def readSingleOption(resultSet: java.sql.ResultSet, pos: Int): Option[org.worldofscala.organisation.LatLonView] =
    val obj = resultSet.getObject(pos, classOf[PGpoint])
    if (resultSet.wasNull()) {
      None
    } else {
      val point = obj.asInstanceOf[PGpoint]
      Some(LatLonView(point.x, point.y))
    }
  override def readSingle(rs: ResultSet, pos: Int): LatLonView =
    val obj = rs.getObject(pos, classOf[PGpoint])
    if (rs.wasNull()) {
      LatLonView.empty
    } else {
      val point = obj.asInstanceOf[PGpoint]
      LatLonView(point.x, point.y)
    }
  override def writeSingle(entity: LatLonView, ps: PreparedStatement, pos: Int): Unit =
    ps.setObject(pos, entity, java.sql.Types.OTHER)
}
