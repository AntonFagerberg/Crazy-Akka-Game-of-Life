# (Crazy) Akka Game of Life
(All of this was written before I really knew anything about Akka so the design is terrible!)

As the title says, this is a crazy implementation of Conway's Game of Life using Akka and Scala. The goal of this implementation was:
 * Use a lot of Actors.
 * Use lots of Futures.
 * Avoid using matrix look-ups for calculating cell neighbours.
 * Not be practical or pragmatic.

## How does it work?
First off, we have the World which is the main supervisor Actor. It will create one cell Actor for each cell and one Checker Actor for each Cell.

The purpose of the Cell Actor is to know if it is alive or not - and if you tell it how many alive neighbours it has, it will die or become alive. (For the single purpose of using a GUI, it will also be aware of its X- & Y-coordinate even though this was not the initial plan). The Cells initial status (dead or alive) is set randomly.

Each Checker Actor will have a sequence of Cell ActorRefs (the neighbours of an unknown Cell) and the purpose of the Checker is to count how many of them are alive and report it to the World Actor. The Checkers are not aware of any positions in the grid.

Each iteration goes like this:
 * World has a collection of all Cells paired with the corresponding Checker.
 * World asks all Checkers to find out how many alive Cells are in their sequence and then waits for all of them to respond (via Futures).
 * Each Checker asks every Cell it knows if it is alive (via Futures).
 * After each Cell has responded, the Checker counts how many are alive and reports the result to the World.
 * After all Checkers has responded, the World will tell every Cell how many alive neighbours is has and ask what status it will have the next iteration (via Futures).
 * Each Cell will live or die and respond with it to the World along with its coordinate.
 * After every Cell has responded, the World will display the result in the GUI and then go again.

## So why is this crazy?
Well, lets say you want to go with a 100 x 100 grid for the Game of Life. This might result in one thread for the World, one thread for each Cell 100*100 = 10,000, one thread for each Checker = 10,000. So we may en up with 20,001 threads. On top of this, each Checker may create 8 Futures and the World may create up to 10,000 Futures at a time - so in a weird worst case scenario, we'll need about 100,000 threads. Your OS and the JVM might have a thing or two to say about this. So ok, you may not end up with 100,000 threads since every actor will probably not consume one thread - but you will end up with a sh*t load of Futures and this will result in way too many threads.

I had a go with a grid of 120x75 which results in 18,001 Actors and it worked fine for about 30 seconds before the thread allocation limit was reached and everything crashed (video: http://instagram.com/p/dMFn6aA7-X/).

## Running it
Use SBT:

```bash
sbt run
```

Change src/main/scala/Main.scala to adjust the number of rows & columns (default is 10x10). You can also change src/main/resources/Application.conf to adjust the Akka settings with number of threads etc. Note that this is also limited by the JVM and your operating system.


### Fake F.A.Q?
 * The Actor system doesn't exit when the GUI does.
 I know, I made the whole GUI / Akka interaction a hack and there are probably a lot of things wrong with it - especially with threads.
