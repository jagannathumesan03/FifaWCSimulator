package com.example.data

object TournamentSeeds {
    val teamsList = listOf(
        // Group A
        Team("MEX", "Mexico", "A", "CONCACAF", 80, "🇲🇽", 15, "Santiago Gimenez", "Possession play, energetic"),
        Team("RSA", "South Africa", "A", "CAF", 72, "🇿🇦", 59, "Percy Tau", "Quick counters, physical midfield"),
        Team("KOR", "South Korea", "A", "AFC", 79, "🇰🇷", 23, "Son Heung-min", "High-intensity workrate, quick shot"),
        Team("CZE", "Czechia", "A", "UEFA", 78, "🇨🇿", 39, "Patrik Schick", "Compact block, direct finishing"),

        // Group B
        Team("CAN", "Canada", "B", "CONCACAF", 79, "🇨🇦", 40, "Alphonso Davies", "Express wing play, overlapping"),
        Team("BIH", "Bosnia and Herzegovina", "B", "UEFA", 75, "🇧🇦", 70, "Edin Dzeko", "Aerial threat, patient possession"),
        Team("QAT", "Qatar", "B", "AFC", 68, "🇶🇦", 48, "Akram Afif", "Counter-attacking, agile movements"),
        Team("SUI", "Switzerland", "B", "UEFA", 81, "🇨🇭", 19, "Granit Xhaka", "Methodical possession, resilient design"),

        // Group C
        Team("BRA", "Brazil", "C", "CONMEBOL", 87, "🇧🇷", 5, "Vinicius Junior", "Creative isolations, attacking width"),
        Team("MAR", "Morocco", "C", "CAF", 81, "🇲🇦", 13, "Achraf Hakimi", "Iron defense, lightning overlap counters"),
        Team("HAI", "Haiti", "C", "CONCACAF", 68, "🇭🇹", 83, "Duckens Nazon", "Direct running, transition attacks"),
        Team("SCO", "Scotland", "C", "UEFA", 76, "🏴", 34, "Scott McTominay", "Physical box-to-box presence, direct"),

        // Group D
        Team("USA", "USA", "D", "CONCACAF", 82, "🇺🇸", 11, "Christian Pulisic", "Fast transition, high-press"),
        Team("PAR", "Paraguay", "D", "CONMEBOL", 76, "🇵🇾", 53, "Miguel Almiron", "Aggressive duels, vertical counters"),
        Team("AUS", "Australia", "D", "AFC", 74, "🇦🇺", 25, "Harry Souttar", "Sturdy backline, set-piece threat"),
        Team("TUR", "Turkiye", "D", "UEFA", 80, "🇹🇷", 27, "Hakan Calhanoglu", "Creative midfield, high-energy pressing"),

        // Group E
        Team("GER", "Germany", "E", "UEFA", 85, "🇩🇪", 16, "Jamal Musiala", "Gegenpressing, fluid dynamic half-spaces"),
        Team("CUW", "Curacao", "E", "CONCACAF", 66, "🇨🇼", 90, "Leandro Bacuna", "Compact defense, counter lanes"),
        Team("CIV", "Ivory Coast", "E", "CAF", 78, "🇨🇮", 41, "Sebastien Haller", "Physical attack, powerful wide play"),
        Team("ECU", "Ecuador", "E", "CONMEBOL", 79, "🇪🇨", 33, "Piero Hincapie", "Athletic wingbacks, high defensive line"),

        // Group F
        Team("NED", "Netherlands", "F", "UEFA", 84, "🇳🇱", 7, "Cody Gakpo", "Total football, high support wingbacks"),
        Team("JPN", "Japan", "F", "AFC", 80, "🇯🇵", 17, "Kaoru Mitoma", "Cohesive pressing, quick link-up play"),
        Team("SWE", "Sweden", "F", "UEFA", 81, "🇸🇪", 24, "Alexander Isak", "Technical build-up, fluid rotation"),
        Team("TUN", "Tunisia", "F", "CAF", 73, "🇹🇳", 41, "Ellyes Skhiri", "Midfield defensive congestion, gritty"),

        // Group G
        Team("BEL", "Belgium", "G", "UEFA", 83, "🇧🇪", 8, "Kevin De Bruyne", "Playmaking excellence, quick transitions"),
        Team("EGY", "Egypt", "G", "CAF", 78, "🇪🇬", 36, "Mohamed Salah", "Counter-attacking, quick wingers"),
        Team("IRN", "Iran", "G", "AFC", 73, "🇮🇷", 20, "Mehdi Taremi", "Deep block, aerial counters"),
        Team("NZL", "New Zealand", "G", "OFC", 65, "🇳🇿", 84, "Chris Wood", "Defensive low-block, set-pieces"),

        // Group H
        Team("ESP", "Spain", "H", "UEFA", 86, "🇪🇸", 3, "Lamine Yamal", "Tiki-taka, wing progression, positional"),
        Team("CPV", "Cape Verde", "H", "CAF", 70, "🇨🇻", 65, "Ryan Mendes", "Organized block, quick wide breaks"),
        Team("KSA", "Saudi Arabia", "H", "AFC", 70, "🇸🇦", 56, "Salem Al-Dawsari", "Compact lines, high offside traps"),
        Team("URU", "Uruguay", "H", "CONMEBOL", 83, "🇺🇾", 14, "Darwin Nunez", "Garra Charrua, chaotic high intensity"),

        // Group I
        Team("FRA", "France", "I", "UEFA", 88, "🇫🇷", 2, "Kylian Mbappe", "Explosive counter-attacks, clinical"),
        Team("SEN", "Senegal", "I", "CAF", 81, "🇸🇳", 18, "Nicolas Jackson", "Physical, swift counter-pressing"),
        Team("IRQ", "Iraq", "I", "AFC", 68, "🇮🇶", 58, "Aymen Hussein", "Aerial threat, deep set playbooks"),
        Team("NOR", "Norway", "I", "UEFA", 82, "🇳🇴", 44, "Erling Haaland", "Power finishing, direct attacking"),

        // Group J
        Team("ARG", "Argentina", "J", "CONMEBOL", 89, "🇦🇷", 1, "Lionel Messi", "Creative vision, high positioning IQ"),
        Team("ALG", "Algeria", "J", "CAF", 77, "🇩🇿", 46, "Riyad Mahrez", "Tricky dribbles, inside-cutting play"),
        Team("AUT", "Austria", "J", "UEFA", 79, "🇦🇹", 25, "Marcel Sabitzer", "Organized pressing, quick combinations"),
        Team("JOR", "Jordan", "J", "AFC", 67, "🇯🇴", 68, "Mousa Al-Taamari", "Compact shape, fast counters"),

        // Group K
        Team("POR", "Portugal", "K", "UEFA", 86, "🇵🇹", 6, "Bruno Fernandes", "High final-third volume, versatile"),
        Team("COD", "DR Congo", "K", "CAF", 72, "🇨🇩", 61, "Yoane Wissa", "Physical counters, strong duels"),
        Team("UZB", "Uzbekistan", "K", "AFC", 70, "🇺🇿", 62, "Eldor Shomurodov", "Disciplined block, quick transitions"),
        Team("COL", "Colombia", "K", "CONMEBOL", 84, "🇨🇴", 12, "Luis Diaz", "Direct attacking, high tempo"),

        // Group L
        Team("ENG", "England", "L", "UEFA", 87, "🏴", 4, "Jude Bellingham", "Structured build-up, versatile attacking"),
        Team("CRO", "Croatia", "L", "UEFA", 83, "🇭🇷", 9, "Luka Modric", "Midfield control, game tempo dictating"),
        Team("GHA", "Ghana", "L", "CAF", 74, "🇬🇭", 64, "Mohammed Kudus", "Direct runs, physical midfields"),
        Team("PAN", "Panama", "L", "CONCACAF", 72, "🇵🇦", 43, "Adalberto Carrasquilla", "High tempo possession build")
    )

    fun getInitialGroupMatches(): List<Match> {
        val matches = mutableListOf<Match>()
        var matchId = 1
        val groups = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")

        for (g in groups) {
            val grpTeams = teamsList.filter { it.group == g }
            if (grpTeams.size == 4) {
                // Circular round robin scheduler
                val t0 = grpTeams[0].id
                val t1 = grpTeams[1].id
                val t2 = grpTeams[2].id
                val t3 = grpTeams[3].id

                // MD 1
                matches.add(createSampleMatch(matchId++, t0, t1, g, "Matchday 1"))
                matches.add(createSampleMatch(matchId++, t2, t3, g, "Matchday 1"))
                // MD 2
                matches.add(createSampleMatch(matchId++, t0, t2, g, "Matchday 2"))
                matches.add(createSampleMatch(matchId++, t1, t3, g, "Matchday 2"))
                // MD 3
                matches.add(createSampleMatch(matchId++, t3, t0, g, "Matchday 3"))
                matches.add(createSampleMatch(matchId++, t1, t2, g, "Matchday 3"))
            }
        }
        return matches
    }

    private fun createSampleMatch(id: Int, tAId: String, tBId: String, grp: String, md: String): Match {
        val teamA = teamsList.first { it.id == tAId }
        val teamB = teamsList.first { it.id == tBId }

        // Dynamic realistic xG bases
        val totalPower = teamA.rating + teamB.rating
        val ratioA = teamA.rating.toFloat() / totalPower
        val xGA = (ratioA * 3.2f).coerceAtLeast(0.5f)
        val xGB = ((1f - ratioA) * 3.2f).coerceAtLeast(0.5f)

        return Match(
            id = id,
            teamAId = tAId,
            teamBId = tBId,
            group = grp,
            isKnockout = false,
            stage = "GROUP",
            date = "$md — Group $grp",
            xGA = String.format("%.1f", xGA).toFloat(),
            xGB = String.format("%.1f", xGB).toFloat()
        )
    }
}
