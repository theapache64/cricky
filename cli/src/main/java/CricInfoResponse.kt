import org.json.JSONObject

class CricInfoResponse(
        private val jsonString: String
) {
    val jResp = JSONObject(jsonString)

    fun getLiveMatches(): List<Match> {
        val matches = mutableListOf<Match>()
        val joMatches = jResp.getJSONObject("matches")
        joMatches.keySet().forEach { matchId ->
            val joMatch = joMatches.getJSONObject(matchId)
            val isLive = joMatch.getString("live_match") == "Y"
            if (isLive) {
                val team1Abbr = joMatch.getString("team1_abbrev")
                val team1Name = joMatch.getString("team1_name")
                val team1Score = joMatch.getString("team1_score")
                val team2Abbr = joMatch.getString("team2_abbrev")
                val team2Name = joMatch.getString("team2_name")
                val team2Score = joMatch.getString("team2_score")

                matches.add(Match(
                        matchId,
                        isLive,
                        team1Abbr,
                        team1Name,
                        team1Score,
                        team2Abbr,
                        team2Name,
                        team2Score
                ))
            }
        }
        return matches
    }

    fun getUpdatedMatch(id: String): Match {

        val joMatches = jResp.getJSONObject("matches")
        val joMatch = joMatches.getJSONObject(id)
        val isLive = joMatch.getString("live_match") == "Y"
        val team1Abbr = joMatch.getString("team1_abbrev")
        val team1Name = joMatch.getString("team1_name")
        val team1Score = joMatch.getString("team1_score")
        val team2Abbr = joMatch.getString("team2_abbrev")
        val team2Name = joMatch.getString("team2_name")
        val team2Score = joMatch.getString("team2_score")

        val match = Match(
                id,
                isLive,
                team1Abbr,
                team1Name,
                team1Score,
                team2Abbr,
                team2Name,
                team2Score
        )
        match.findScore()
        return match
    }

}