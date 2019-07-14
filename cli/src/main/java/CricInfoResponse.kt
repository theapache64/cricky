import org.json.JSONObject

class CricInfoResponse(
        jsonString: String
) {
    private val jResp = JSONObject(jsonString)

    fun getLiveMatches(): List<Match> {
        val matches = mutableListOf<Match>()
        val joMatches = jResp.getJSONObject("matches")
        joMatches.keySet().forEach { matchId ->
            val match = Match.parse(matchId, joMatches)
            if (match.isLiveMatch) {
                matches.add(match)
            }
        }
        return matches
    }

    fun getUpdatedMatch(id: String): Match {
        val joMatches = jResp.getJSONObject("matches")
        val match = Match.parse(id, joMatches)
        match.findScore()
        return match
    }

}