import kotlin.random.Random
import kotlin.system.exitProcess

enum class GameState {
    NOT_STARTED, STARTED, BATS, HOLE, WUMPUS_KILL, WUMPUS_WAKE, NO_ARROWS, WIN
}

var debugMode = false
var gameState = GameState.STARTED     // поменять по умолчанию на NOT_STARTED

val roomsFree: MutableList<Int> = mutableListOf()
val roomsFilled: MutableList<Int> = mutableListOf()

val bats: MutableList<Int> = mutableListOf()
val holes: MutableList<Int> = mutableListOf()
var wumpus: Int = -1
var player: Int = -1
var arrows = 5

val neighbors = arrayOf(
        listOf(1, 4, 7),
        listOf(0, 2, 5),
        listOf(1, 3, 13),
        listOf(2, 4, 11),
        listOf(0, 3, 9),
        listOf(1, 6, 14),
        listOf(5, 7, 16),
        listOf(0, 6, 8),
        listOf(7, 9, 17),
        listOf(4, 8, 10),
        listOf(9, 11, 18),
        listOf(3, 10, 12),
        listOf(11, 13, 19),
        listOf(2, 12, 14),
        listOf(5, 13, 15),
        listOf(14, 16, 19),
        listOf(6, 15, 17),
        listOf(8, 16, 18),
        listOf(10, 17, 19),
        listOf(12, 15, 18)
)

fun initMap() {
    for (i in 0..19) {
        roomsFree.add(i)
    }
    fillRoom(bats, getFreeRoom())
    fillRoom(bats, getFreeRoom())
    fillRoom(holes, getFreeRoom())
    fillRoom(holes, getFreeRoom())

    wumpus = getFreeRoom()
    fillRoom(wumpus)

    player = getFreeRoom()
    fillRoom(player)
}

fun getFreeRoom(): Int {
    return roomsFree[Random.nextInt(roomsFree.size)]
}

fun getRandomRoom(): Int {
    return (0..19).map { i -> i }[Random.nextInt(20)]
}

fun fillRoom(list: MutableList<Int>, r: Int): Int {
    list.add(r)
    roomsFilled.add(r)
    roomsFree.remove(r)
    return r
}

fun fillRoom(r: Int) {
    roomsFilled.add(r)
    roomsFree.remove(r)
}

fun emptyRoom(r: Int) {
    roomsFree.add(r)
    roomsFilled.remove(r)
}

fun getNeighbors(r: Int): List<Int> {
    return neighbors[r]
}

fun printState() {
    if (debugMode) {
        println("Bats: ${bats+1}")
        println("Holes: ${holes+1}")
        println("Wumpus: ${wumpus+1}")
        println("Player: ${player+1}")
        println("------------------------")
    }

    val n = getNeighbors(player)

    println("You have $arrows arrow${if(arrows == 1) "" else "s"}")
    println("You're in the room ${player+1}")
    println("Tunnels to rooms ${(n.map{ x -> x+1})}")

    if (bats.intersect(n).isNotEmpty()) {
        println("Bats near!")
    }
    if (holes.intersect(n).isNotEmpty()) {
        println("Hole near!")
    }
    if (n.contains(wumpus)) {
        println("Wumpus near!")
    }
    println("==================")
}

fun moveTo(r: Int) {
    gameState = GameState.STARTED
    emptyRoom(player)
    player = r
    when {
        player == wumpus -> gameState = GameState.WUMPUS_KILL
        holes.contains(r) -> gameState = GameState.HOLE
        bats.contains(r) -> gameState = GameState.BATS
    }
}

fun shootTo(r: Int) {
    if (!neighbors[player].contains(r)){
        return
    }
    gameState = GameState.STARTED
    arrows--

    when {
        wumpus == r -> gameState = GameState.WIN
        arrows == 0 -> gameState = GameState.NO_ARROWS
        Random.nextDouble(1.0) > 0.25 -> gameState = GameState.WUMPUS_WAKE
    }
}

fun moveBats() {
    bats.remove(player)
    bats.add(getFreeRoom())
    moveTo(getRandomRoom())
}

fun moveWumpus() {
    gameState = GameState.STARTED
    val n = getNeighbors(wumpus)
    emptyRoom(wumpus)
    wumpus = n[Random.nextInt(3)]
    if (wumpus == player) {
        gameState = GameState.WUMPUS_KILL
    }
}

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0].trim().toLowerCase() == "debug") {
        debugMode = true
    }

    initMap()

    while(true) {
        printState()
        when (gameState) {
            GameState.WUMPUS_KILL -> {
                println("Wumpus ates you!\nYOU DIED")
                exitProcess(0)
            }
            GameState.WUMPUS_WAKE -> {
                println("Wumpus woke up!")
                moveWumpus()
            }
            GameState.BATS -> {
                println("Bats are turning you to another room!")
                moveBats()
            }
            GameState.HOLE -> {
                println("You fell into hole!\nYOU DIED")
                exitProcess(0)
            }
            GameState.NO_ARROWS -> {
                println("You're out of arrows!\nYOU LOSE")
                exitProcess(0)
            }
            GameState.WIN -> {
                println("Wumpus is dead!\n*** YOU WIN! ***")
                exitProcess(0)
            }
            else -> {
                print("> ")
                val cmd = (readLine() ?: "").trim().toUpperCase()
                when {
                    cmd.matches("^M\\d+$".toRegex()) -> moveTo(cmd.drop(1).toInt()-1)
                    cmd.matches("^S\\d+$".toRegex()) -> shootTo(cmd.drop(1).toInt()-1)
                    cmd.matches("^Q$".toRegex()) -> {
                        println("Bye.")
                        exitProcess(0)
                    }
                }
            }
        }
    }
}