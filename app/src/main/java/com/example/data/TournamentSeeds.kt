package com.example.data

object TournamentSeeds {
    val teamsList = listOf(
        // Group A
        Team("USA", "United States", "A", "CONCACAF", 82, "🇺🇸", 11, "Christian Pulisic", "Fast transition, high-press"),
        Team("COL", "Colombia", "A", "CONMEBOL", 84, "🇨🇴", 12, "Luis Díaz", "Direct attacking, high tempo"),
        Team("POL", "Poland", "A", "UEFA", 78, "🇵🇱", 28, "Robert Lewandowski", "Target-man focus, physical"),
        Team("NZL", "New Zealand", "A", "OFC", 65, "🇳🇿", 84, "Chris Wood", "Defensive low-block, set-pieces"),

        // Group B
        Team("CAN", "Canada", "B", "CONCACAF", 79, "🇨🇦", 40, "Alphonso Davies", "Express wing play, overlapping"),
        Team("SWE", "Sweden", "B", "UEFA", 81, "🇸🇪", 24, "Alexander Isak", "Technical build-up, fluid rotation"),
        Team("EGY", "Egypt", "B", "CAF", 78, "🇪🇬", 36, "Mohamed Salah", "Counter-attacking, quick wingers"),
        Team("KOR", "South Korea", "B", "AFC", 79, "🇰🇷", 23, "Son Heung-min", "High-intensity workrate, quick shot"),

        // Group C
        Team("MEX", "Mexico", "C", "CONCACAF", 80, "🇲🇽", 15, "Santiago Giménez", "Possession play, energetic"),
        Team("DEN", "Denmark", "C", "UEFA", 82, "🇩🇪", 21, "Rasmus Højlund", "Compact organization, wing crosses"),
        Team("SEN", "Senegal", "C", "CAF", 81, "🇸🇳", 18, "Nicolas Jackson", "Physical, swift counter-pressing"),
        Team("AUS", "Australia", "C", "AFC", 74, "🇦🇺", 25, "Harry Souttar", "Sturdy backline, set-piece threat"),

        // Group D
        Team("FRA", "France", "D", "UEFA", 88, "🇫🇷", 2, "Kylian Mbappé", "Explosive counter-attacks, clinical"),
        Team("SUI", "Switzerland", "D", "UEFA", 81, "🇨🇭", 19, "Granit Xhaka", "Methodical possession, resilient design"),
        Team("CHI", "Chile", "D", "CONMEBOL", 77, "🇨🇱", 42, "Alexis Sánchez", "High intensity, high tactical line"),
        Team("IRN", "Iran", "D", "AFC", 73, "🇮🇷", 20, "Mehdi Taremi", "Deep block, aerial counters"),

        // Group E
        Team("BRA", "Brazil", "E", "CONMEBOL", 87, "🇧🇷", 5, "Vinícius Júnior", "Joga bonito, creative isolations"),
        Team("NED", "Netherlands", "E", "UEFA", 84, "🇳🇱", 7, "Cody Gakpo", "Total football, high support wingbacks"),
        Team("ALG", "Algeria", "E", "CAF", 77, "🇩🇿", 46, "Riyad Mahrez", "Tricky dribbles, inside-cutting play"),
        Team("JAM", "Jamaica", "E", "CONCACAF", 72, "🇯🇲", 54, "Leon Bailey", "Pace-heavy counters, physical battles"),

        // Group F
        Team("ENG", "England", "F", "UEFA", 87, "🏴󠁧󠁢󠁥󠁮󠁧󠁿", 4, "Jude Bellingham", "Structured build-up, versatile attacking"),
        Team("CRO", "Croatia", "F", "UEFA", 83, "🇭🇷", 9, "Luka Modrić", "Midfield control, game tempo dictating"),
        Team("PER", "Peru", "F", "CONMEBOL", 76, "🇵🇪", 31, "Gianluca Lapadula", "Gritty, direct football, high-press"),
        Team("JPN", "Japan", "F", "AFC", 80, "🇯🇵", 17, "Kaoru Mitoma", "Cohesive pressing, quick link-up play"),

        // Group G
        Team("ARG", "Argentina", "G", "CONMEBOL", 89, "🇦🇷", 1, "Lionel Messi", "Creative vision, high positioning IQ"),
        Team("GER", "Germany", "G", "UEFA", 85, "🇩🇪", 16, "Jamal Musiala", "Gegenpressing, fluid dynamic half-spaces"),
        Team("NGA", "Nigeria", "G", "CAF", 78, "🇳🇬", 30, "Victor Osimhen", "Direct verticality, physical focal striker"),
        Team("KSA", "Saudi Arabia", "G", "AFC", 70, "🇸🇦", 56, "Salem Al-Dawsari", "Compact lines, high offside traps"),

        // Group H
        Team("ESP", "Spain", "H", "UEFA", 86, "🇪🇸", 3, "Lamine Yamal", "Tiki-taka, wing progression, positional"),
        Team("ITA", "Italy", "H", "UEFA", 84, "🇮🇹", 10, "Federico Chiesa", "Tactical flexibility, solid back three"),
        Team("MAR", "Morocco", "H", "CAF", 81, "🇲🇦", 13, "Achraf Hakimi", "Iron defense, lightning overlap counters"),
        Team("CRC", "Costa Rica", "H", "CONCACAF", 71, "🇨🇷", 50, "Joel Campbell", "Low block, disciplined target defense"),

        // Group I
        Team("POR", "Portugal", "I", "UEFA", 86, "🇵🇹", 6, "Bruno Fernandes", "High final-third volume, versatile"),
        Team("URU", "Uruguay", "I", "CONMEBOL", 83, "🇺🇾", 14, "Darwin Núñez", "Garra Charrúa, chaotic high intensity"),
        Team("SRB", "Serbia", "I", "UEFA", 77, "🇷🇸", 32, "Dušan Vlahović", "Physical wings, heavy aerial crosses"),
        Team("QAT", "Qatar", "I", "AFC", 68, "🇶🇦", 48, "Akram Afif", "Counter-attacking, agile movements"),

        // Group J
        Team("BEL", "Belgium", "J", "UEFA", 83, "🇧🇪", 8, "Kevin De Bruyne", "Playmaking excellence, quick transitions"),
        Team("UKR", "Ukraine", "J", "UEFA", 78, "🇺🇦", 22, "Artem Dovbyk", "Direct vertical counters, rigid defense"),
        Team("GHA", "Ghana", "J", "CAF", 74, "🇬🇭", 64, "Mohammed Kudus", "Direct runs, physical midfields"),
        Team("UAE", "United Arab Emirates", "J", "AFC", 66, "🇦🇪", 68, "Fabio Lima", "Slow build-up, conservative block"),

        // Group K
        Team("ECU", "Ecuador", "K", "CONMEBOL", 79, "🇪🇨", 33, "Piero Hincapié", "Athletic wingbacks, high defensive line"),
        Team("WAL", "Wales", "K", "UEFA", 76, "🏴󠁧󠁢󠁷󠁬󠁳󠁿", 29, "Brennan Johnson", "Direct long-ball, speed transitions"),
        Team("TUN", "Tunisia", "K", "CAF", 73, "🇹🇳", 41, "Ellyes Skhiri", "Midfield defensive congestion, gritty"),
        Team("IRQ", "Iraq", "K", "AFC", 68, "🇮🇶", 58, "Aymen Hussein", "Aerial threat, deep set playbooks"),

        // Group L
        Team("SCO", "Scotland", "L", "UEFA", 76, "🏴󠁧󠁢󠁳󠁣󠁴󠁿", 34, "Scott McTominay", "Physical box-to-box presence, direct"),
        Team("CMR", "Cameroon", "L", "CAF", 75, "🇨🇲", 49, "André Onana", "Sweeplay goalie builds, athletic press"),
        Team("AUT", "Austria", "L", "UEFA", 79, "🇦🇺", 25, "Marcel Sabitzer", "Organized redbull-style counter pressing"),
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
