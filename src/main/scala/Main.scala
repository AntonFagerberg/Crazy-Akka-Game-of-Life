package scala

import akka.actor.{ActorSystem, Props}

object Main extends App {
  val (rows, columns, cellSize) = (20, 20, 10)
  val system = ActorSystem("System")
  val gui = new Gui(columns * cellSize, rows * cellSize)

  val world =
    system.actorOf(
      Props(
        new World(
          columns,
          rows,
          cellSize,
          gui
        )
      ).withDispatcher("gameoflife-dispatcher"),
      "world"
    )
}
