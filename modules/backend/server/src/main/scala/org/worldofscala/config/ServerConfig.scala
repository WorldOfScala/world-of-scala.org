package org.worldofscala.config

import zio.Config
import zio.ZLayer
import zio.config.magnolia.deriveConfig

final case class ServerConfig(port: Int)

object ServerConfig:
  given Config[ServerConfig] = deriveConfig[ServerConfig]

  def layer: ZLayer[Any, Config.Error, ServerConfig] = Configs.makeConfigLayer[ServerConfig]("server")
