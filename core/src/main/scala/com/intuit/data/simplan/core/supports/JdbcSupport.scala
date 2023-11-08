package com.intuit.data.simplan.core.supports

import com.intuit.data.simplan.core.context.Support
import com.intuit.data.simplan.global.domain.QualifiedParam
import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager}

/**
  * @author Abraham, Thomas - tabraham1
  *         Created on 05-Jan-2022 at 1:36 PM
  */

case class JdbcConfig(name: Option[String], driver: String, url: String, username: String, password: QualifiedParam) extends Serializable {
  def resolvedName = name.getOrElse("RDBMS")
}

trait JdbcSupport extends Support {
  @transient private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private val JDBC_CONFIG_KEY = "jdbc"
  private val jdbcConfig: JdbcConfig = appContextConfig.getSystemConfigAs[JdbcConfig](JDBC_CONFIG_KEY)

  private def createJdbcConnection(jdbcConfig: JdbcConfig): Connection = {
    val driver: String = jdbcConfig.driver
    val url: String = jdbcConfig.url
    val username: String = jdbcConfig.username
    val password = jdbcConfig.password
    Class.forName(driver)
    DriverManager.getConnection(url, username, password.resolve)
  }

  lazy val jdbcConnection: Connection = {
    logger.info(s"Trying to establish JDBC Connection to ${jdbcConfig.resolvedName}")
    val connection = createJdbcConnection(jdbcConfig)
    logger.info(s"JDBC Connection established to ${jdbcConfig.resolvedName}")
    connection
  }
}
