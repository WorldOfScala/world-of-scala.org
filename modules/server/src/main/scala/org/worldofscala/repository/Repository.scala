package org.worldofscala.repository

import zio.*
import com.augustnagro.magnum.*
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import javax.sql.DataSource
import java.util.UUID

object Repository {

  def datasourceLayer: TaskLayer[DataSource] = ZLayer.scoped {
    ZIO.fromAutoCloseable {
      ZIO.attempt {
        val config = new HikariConfig()
        config.setJdbcUrl(sys.env.getOrElse("DB_JDBC_URL", "jdbc:postgresql://localhost:5432/world-of-scala"))
        config.setUsername(sys.env.getOrElse("DB_USER", "docker"))
        config.setPassword(sys.env.getOrElse("DB_PASSWORD", "docker"))
        config.setMaximumPoolSize(10)
        new HikariDataSource(config)
      }
    }
  }

  def dataLayer: TaskLayer[DataSource] = datasourceLayer
}

trait UUIDMapper[A](a2id: A => UUID, id2a: UUID => A) {
  given DbCodec[A] = DbCodec.UUIDCodec.biMap[A](id2a, a2id)
}
