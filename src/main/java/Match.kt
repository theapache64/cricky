class Match(
        val id: String,
        val isLiveMatch: Boolean,
        val team1Abbr: String,
        val team1Name: String,
        val team1Score: String,
        val team2Abbr: String,
        val team2Name: String,
        val team2Score: String
) {


    var team1Wickets: Int = 0
    var team2Wickets: Int = 0
    var team2Runs: Int = 0
    var team1Runs: Int = 0

    fun findScore() {
        if (team1Score.isNotEmpty()) {
            this.team1Runs = team1Score.split("/")[0].trim().toInt()
            this.team1Wickets = team1Score.split("/")[1].split(" ")[0].trim().toInt()
            val team1OversFaced = team1Score.split("(")[1].split("/")[0]
            val team1TotalOvers = team1Score.split("/")[2].split("&")[0]
        }

        if (team2Score.isNotEmpty()) {
            this.team2Runs = team2Score.split("/")[0].trim().toInt()
            this.team2Wickets = team2Score.split("/")[1].split(" ")[0].trim().toInt()
            val team2OversFaced = team2Score.split("(")[1].split("/")[0]
            val team2TotalOvers = team2Score.split("/")[2].split("&")[0]
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
}