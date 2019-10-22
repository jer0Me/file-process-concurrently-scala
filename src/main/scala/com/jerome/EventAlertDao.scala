package com.jerome

import scala.concurrent.ExecutionContext
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.hikari.HikariTransactor

object EventAlertDao {

  private implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val driverClassName = "org.h2.Driver"
  private val databaseUrl = "jdbc:h2:~/test;DB_CLOSE_DELAY=-1"
  private val username = "sa"
  private val password = "sa"

  private val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[IO](32)
      blocker <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = driverClassName,
        url = databaseUrl,
        user = username,
        pass = password,
        connectEC = connectEC,
        blocker = blocker
      )
    } yield xa

  def initTable: IO[Unit] = {
    val drop = sql"""
      DROP TABLE IF EXISTS event_alert
    """.update.run

    val create = sql"""
      CREATE TABLE event_alert (
      id       BIGINT AUTO_INCREMENT,
      event_id VARCHAR NOT NULL,
      duration INTEGER,
      type VARCHAR(255),
      host VARCHAR(255),
      alert SMALLINT
    )
    """.update.run

     for {
      _ <- transactor.use { xa =>
             drop.transact(xa)
           }
      _ <- transactor.use { xa =>
             create.transact(xa)
           }
    } yield ()
  }

  def saveEventAlert(eventLogAlert: EventLogAlert): IO[Unit] = {
    transactor.use { xa =>
      val yolo = xa.yolo
      import yolo._

      insertEventAlert(
        eventId = eventLogAlert.id,
        duration = eventLogAlert.duration,
        logType = eventLogAlert.logType,
        host = eventLogAlert.host,
        isAlert = eventLogAlert.isAlert
      ).quick
    }
  }

  private def insertEventAlert(eventId: String,
                               duration: Int,
                               logType: Option[String],
                               host: Option[String],
                               isAlert: Boolean): Update0 =

    sql"""insert into event_alert (event_id, duration, type, host, alert)
                values ($eventId, $duration, $logType, $host, $isAlert)
        """.update

}

