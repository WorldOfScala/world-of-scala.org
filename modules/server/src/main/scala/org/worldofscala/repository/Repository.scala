package org.worldofscala.repository

import com.augustnagro.magnum.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import zio.*

import java.util.UUID
import javax.sql.DataSource

def datasourceLayer: TaskLayer[DataSource] = ZLayer.scoped {
  ZIO.fromAutoCloseable {
    ZIO.attempt {
      val config = new HikariConfig()
      config.setJdbcUrl(sys.env.getOrElse("DATABASE_JDBC_URL", "jdbc:postgresql://localhost:5432/world-of-scala"))
      config.setUsername(sys.env.getOrElse("DATABASE_USER", "docker"))
      config.setPassword(sys.env.getOrElse("DATABASE_PASSWORD", "docker"))
      config.setMaximumPoolSize(10)
      new HikariDataSource(config)
    }
  }
}

def dataLayer: TaskLayer[DataSource] = datasourceLayer

trait UUIDMapper[A](a2id: A => UUID, id2a: UUID => A) {
  given DbCodec[A] = DbCodec.UUIDCodec.biMap[A](id2a, a2id)
}
