package scala

import akka.actor.{Props, Actor}
import akka.pattern.ask
import scala.util.Random
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.collection.mutable.ArrayBuffer

class World(columns: Int, rows: Int, cellSize: Int, gui: Gui) extends Actor {
  implicit val timeout = Timeout(5 seconds)
  val random = new Random()
  val paintBuffer = new ArrayBuffer[(Int, Int, Int, Int)]

  val cells = {
    for {
      column <- 0 until columns
      row <- 0 until rows
    } yield {
      (column -> row) -> context.actorOf(Props(new Cell(self, random.nextBoolean, column, row)))
    }
  }.toMap

  val checkers =
    for (((cellRow, cellColumn), cell) <- cells) yield {
      val neighbours =
        for {
          neighbourX <- cellRow - 1 to cellRow + 1 if neighbourX >= 0 && neighbourX < columns
          neighbourY <- cellColumn - 1 to cellColumn + 1 if neighbourY >= 0 && neighbourY < rows && (cellRow, cellColumn) != (neighbourX, neighbourY)
        } yield {
          cells(neighbourX, neighbourY)
        }

      cell -> context.actorOf(Props(new Checker(neighbours)))
    }

  override def preStart(): Unit = {
    self ! "tick"
  }

  def receive = {
    case "tick" => {
      paintBuffer.clear()

      checkers.map { case(cell, checker) =>
        cell -> checker ? "check"
      }.map { case(cell, response) =>
        cell -> Await.result(response, timeout.duration).asInstanceOf[Int]
      }.map { case(cell, neighbours) =>
        cell ? neighbours
      }.foreach{ response =>
        val (alive, x, y) = Await.result(response, timeout.duration).asInstanceOf[(Boolean, Int, Int)]

        if (alive) {
          paintBuffer += ((x * cellSize, y * cellSize, cellSize, cellSize))
        }
      }

      gui.Paint.updateCoordinates(paintBuffer.clone())
      self ! "tick"
    }

    case _ => context.stop(self)
  }
}
