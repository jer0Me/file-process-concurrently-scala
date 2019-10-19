package com.jerome

import cats.effect.{IO, Resource}
import cats.syntax.applicative._
import scala.io.Source

object EventLogFileReader {

  def process(fileName: String): Unit = {
    Resource
      .fromAutoCloseable(Source.fromFile(fileName).pure[IO])
      .use(buffer => EventLogProcessor.processEventLogLines(buffer.getLines()))
      .unsafeRunSync
  }

}
