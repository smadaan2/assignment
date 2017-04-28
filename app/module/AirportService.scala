package module

import csv.CsvReader

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AirportOps {
  val fileName: String

  val airports: Vector[Airport]

  def getAirports(country: Country): Future[Vector[Airport]]

  def getRunways(airport: Airport): Future[Vector[Runway]]

  def getTypeOfRunways(airport: Airport): Future[Vector[String]]

  def totalRunways(airport: Airport): Future[Int]
}

class AirportDefaultOps(runwayService: RunwayDefaultOps) extends AirportOps {
  override lazy val fileName: String = "conf/data/airports.csv"

  override lazy val airports: Vector[Airport] = new CsvReader(fileName)
    .readAndParse((line, headers) => Airport(line, headers))

  def getRunways(airport: Airport): Future[Vector[Runway]] = runwayService.getRunways(airport)

  def getTypeOfRunways(airport: Airport): Future[Vector[String]] = getRunways(airport).map {
    runways =>
      runways.map(_.surface)
  }

  def totalRunways(airport: Airport): Future[Int] = getRunways(airport).map(_.length)

  def getAirports(country: Country): Future[Vector[Airport]] = Future {
    val code = country.code.toLowerCase
    val filter = (airport: Airport) => airport.isoCountry.toLowerCase == code
    airports.filter(filter)
  }
}

object AirportService extends AirportDefaultOps(new RunwayDefaultOps)