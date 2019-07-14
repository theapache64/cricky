import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.awt.Desktop
import java.net.SocketTimeoutException
import java.net.URI
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
    client.setReadTimeout(1, TimeUnit.MINUTES)
    client.setWriteTimeout(1, TimeUnit.MINUTES)
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

    /*println("Who's batting ?")
    println("1) ${match.team1Name}")
    println("2) ${match.team2Name}")

    val batting = scanner.nextInt()
    if (batting != 1 && batting != 2) {
        println("Wrong option!!")
        whosBatting(match)
        return
    }*/

    val isTeam1Batting = match.team1Score.contains("&nbsp;")
    println("Batting team is ${if (isTeam1Batting) {
        match.team1Name
    } else {
        match.team2Name
    }}")

    prevScore = match
    prevScore!!.findScore()

    askRefreshInterval(match, isTeam1Batting)
}

fun askRefreshInterval(match: Match, isTeam1Batting: Boolean) {
    print("Refresh interval in seconds : ")
    val refreshInterval = scanner.nextInt()
    if (refreshInterval < 1) {
        println("Refresh interval must greater than zero")
        askRefreshInterval(match, isTeam1Batting)
        return
    }
    println("Refresh interval : $refreshInterval second(s)")

    val refreshIntervalInMillis = refreshInterval * 1000L

    try {

        watch(match, isTeam1Batting, refreshIntervalInMillis)
    } catch (e: SocketTimeoutException) {
        println("Timeout from API")
        watch(match, isTeam1Batting, refreshIntervalInMillis)
    }
}

fun watch(match: Match, isTeam1Batting: Boolean, updateIntervalInMillis: Long) {
    println("-------------------------")

    val response = client.newCall(request).execute()
    val cricInfo = CricInfoResponse(response.body().string())
    val updatedMatch = cricInfo.getUpdatedMatch(match.id)
    val notification = getNotification(updatedMatch, prevScore!!, isTeam1Batting)
    if (notification.first != NotificationType.NOTHING) {
        notify(notification.first.title, notification.second)
        playSound()
        openDefaultBrowser(match.url)
    } else {
        println("nothing significant happened")
    }

    prevScore = updatedMatch
    Thread.sleep(updateIntervalInMillis)
    watch(match, isTeam1Batting, updateIntervalInMillis)
}

fun openDefaultBrowser(url: String) {
    Desktop.getDesktop().browse(URI(url))
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
