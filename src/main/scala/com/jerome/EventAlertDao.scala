package com.jerome

import scala.concurrent.ExecutionContext
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.hikari.HikariTransactor

object EventAlertDao {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val driverClassName = "org.h2.Driver"
  val databaseUrl = "jdbc:h2:~/test;DB_CLOSE_DELAY=-1"
  val username = "sa"
  val password = "sa"

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      be <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](driverClassName, databaseUrl, username, password, ce, be)
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

      - <- transactor.use { xa =>
            create.transact(xa)
           }
    } yield ()
  }

  def saveEventAlert(eventLogAlert: EventLogAlert): IO[Int] = {
    transactor.use { xa =>
      insertEventAlert(
        eventLogAlert.id,
        eventLogAlert.duration,
        eventLogAlert.logType,
        eventLogAlert.host,
        eventLogAlert.isAlert
      ).run.transact(xa)
    }
  }

  def insertEventAlert(eventId: String, duration: Int, logType: Option[String], host: Option[String], isAlert: Boolean): Update0 =
    sql"""insert into event_alert (event_id, duration, type, host, alert)
                values ($eventId, $duration, $logType, $host, $isAlert)
        """.update

}

