import org.json.JSONObject

class Match(
        val id: String,
        val isLiveMatch: Boolean,
        val team1Abbr: String,
        val team1Name: String,
        val team1Score: String,
        val team2Abbr: String,
        val team2Name: String,
        val team2Score: String,
        url: String
) {


    val url = "https://espncricinfo.com$url"
    var team1Wickets: Int = 0
    var team2Wickets: Int = 0
    var team2Runs: Int = 0
    var team1Runs: Int = 0

    fun findScore() {
        if (team1Score.isNotEmpty()) {
            this.team1Runs = team1Score.split("/")[0].trim().toInt()
            this.team1Wickets = team1Score.split("/")[1].split(" ")[0].trim().toInt()
        }

        if (team2Score.isNotEmpty()) {
            this.team2Runs = team2Score.split("/")[0].trim().toInt()
            this.team2Wickets = team2Score.split("/")[1].split(" ")[0].trim().toInt()
        }
    }

    fun getRunsDifference(prevScore: Match, team1Batting: Boolean): Int {
        if (team1Batting) {
            return team1Runs - prevScore.team1Runs
        } else {
            return team2Runs - prevScore.team2Runs
        }

    }

    fun getWicketsDiff(prevScore: Match, team1Batting: Boolean): Int {
        if (team1Batting) {
            return team1Wickets - prevScore.team1Wickets
        } else {
            return team2Wickets - prevScore.team2Wickets
        }
    }

    companion object {
        fun parse(id: String, joMatches: JSONObject): Match {
            val joMatch = joMatches.getJSONObject(id)
            val isLive = joMatch.getString("live_match") == "Y"
            val team1Abbr = joMatch.getString("team1_abbrev")
            val team1Name = joMatch.getString("team1_name")
            val team1Score = joMatch.getString("team1_score")
            val team2Abbr = joMatch.getString("team2_abbrev")
            val team2Name = joMatch.getString("team2_name")
            val team2Score = joMatch.getString("team2_score")
            val url = joMatch.getString("url")

            return Match(
                    id,
                    isLive,
                    team1Abbr,
                    team1Name,
                    team1Score,
                    team2Abbr,
                    team2Name,
                    team2Score,
                    url
            )
        }
    }
}