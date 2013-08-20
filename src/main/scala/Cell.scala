package scala

import akka.actor.{ActorRef, Actor}

class Cell(world: ActorRef, var alive: Boolean, column: Int, row: Int) extends Actor {
  def receive = {
    case aliveNeighbours: Int => {
      alive = aliveNeighbours == 3 || (alive && aliveNeighbours == 2)
      sender ! (alive, column, row)
    }

    case "alive" => sender ! alive
  }
}