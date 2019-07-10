import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import okio.Timeout
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit

val scanner = Scanner(System.`in`)
var prevScore: Match? = null

val client = OkHttpClient()

val request = Request.Builder()
        .url("http://api.espncricinfo.com/netstorage/summary.json")
        .get()
        .build()

fun main(args: Array<String>) {
    client.setConnectTimeout(1, TimeUnit.MINUTES)
    client.retryOnConnectionFailure = true
    askMatchDetails()
}

fun notify(title: String, message: String) {
    Runtime.getRuntime().exec(arrayOf(
            "/usr/bin/notify-send",
            title,
            message
    ));
}


private fun askMatchDetails() {

    val response = client.newCall(request).execute()
    val cricInfo = CricInfoResponse(response.body().string())
    val liveMatches = cricInfo.getLiveMatches()

    if (liveMatches.isEmpty()) {
        println("No live match found")
    } else {
        chooseMatch(liveMatches)
    }
}

fun chooseMatch(liveMatches: List<Match>) {

    println("Choose a match")
    liveMatches.forEachIndexed { index, match ->
        println("${index + 1}) ${match.team1Name} vs ${match.team2Name}")
    }
    print("Enter match number :")
    val matchNum = scanner.nextInt()
    if (matchNum < 1 || matchNum > liveMatches.size) {
        println("Unknown match : $matchNum")
        chooseMatch(liveMatches)
        return
    }

    val match = liveMatches[matchNum - 1] // to match index -1
    println("Watching ${match.team1Name} vs ${match.team2Name} ...")

    whosBatting(match)
}

private fun whosBatting(match: Match) {

    println("Who's batting ?")
    println("1) ${match.team1Name}")
    println("2) ${match.team2Name}")

    val batting = scanner.nextInt()
    if (batting != 1 && batting != 2) {
        println("Wrong option!!")
        whosBatting(match)
        return
    }

    prevScore = match
    prevScore!!.findScore()
    try {
        watch(match, batting == 1)
    } catch (e: SocketTimeoutException) {
        println("Timeout from API")
        watch(match, batting == 1)
    }
}

fun watch(match: Match, isTeam1Batting: Boolean) {
    println("-------------------------")

    val response = client.newCall(request).execute()
    val cricInfo = CricInfoResponse(response.body().string())
    val updatedMatch = cricInfo.getUpdatedMatch(match.id)
    val notification = getNotification(updatedMatch, prevScore!!, isTeam1Batting)
    if (notification.first != NotificationType.NOTHING) {
        playSound()
        notify(notification.first.title, notification.second)
    } else {
        println("nothing significant happened")
    }

    prevScore = updatedMatch
    Thread.sleep(5000)
    watch(match, isTeam1Batting)
}

private fun playSound() {
    Thread {
        Runtime.getRuntime().exec(arrayOf(
                "/usr/bin/mplayer",
                "plucky.mp3"
        ));
    }.start()
}

fun getNotification(newMatch: Match, prevScore: Match, isTeam1Batting: Boolean): Pair<NotificationType, String> {

    val battingTeamName = if (isTeam1Batting) {
        newMatch.team1Name
    } else {
        newMatch.team2Name
    }

    val runsDiff = newMatch.getRunsDifference(prevScore, isTeam1Batting)

    println("Runs diff is $runsDiff")

    if (runsDiff == 4) {
        return Pair(NotificationType.FOUR, "$battingTeamName hit a FOUR!")
    }

    if (runsDiff == 6) {
        return Pair(NotificationType.SIX, "$battingTeamName hit a SIX!!")
    }

    if (runsDiff > 4) {
        return Pair(NotificationType.WHAT, "$battingTeamName scored some notable runs")
    }

    val wicketsDiff = newMatch.getWicketsDiff(prevScore, isTeam1Batting)

    println("Wickets diff is $wicketsDiff")

    if (wicketsDiff > 0) {
        return Pair(NotificationType.OUT, "$battingTeamName lost a wicket")
    }

    return Pair(NotificationType.NOTHING, "")
}
