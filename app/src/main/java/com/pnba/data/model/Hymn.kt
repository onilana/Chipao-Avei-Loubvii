package com.pnba.data.model

data class HymnVerse(
    val verseNumber: Int,
    val lines: List<String>
)

data class Hymn(
    val id: Int,
    val title: String,                  // Poula Title (e.g. "O VEO THO LAIKHE")
    val englishTitle: String = "",       // English Title (e.g. "O Worship The King")
    val firstLine: String = "",
    val timeSignature: String = "",      // e.g. "3/4", "4/4"
    val keySignature: String = "",       // Tonic Solfa key: e.g. "Doh is A"
    val hymnBookRef: String = "",        // e.g. "C.W.H. 94", "S.S. 23"
    val author: String = "",
    val composer: String = "",
    val scripture: String = "",
    val category: String = "Worship",
    val categories: List<String> = listOf(category),
    val verses: List<HymnVerse> = emptyList(),
    val chorus: List<String>? = null,
    val keynotePitchHz: Float = 440f,
    val keynoteName: String = "A",
    val melodyNotes: List<MelodyNote> = emptyList(),
    val reflection: String = "",
    val tune: String = if (englishTitle.isNotBlank()) englishTitle else hymnBookRef,
    val meter: String = timeSignature,
    val year: String = ""
) {
    fun belongsToCategory(cat: String): Boolean {
        if (cat.isBlank() || cat.equals("All", ignoreCase = true) || cat.equals("All Categories", ignoreCase = true)) {
            return true
        }
        val cleanCat = cat.replace(Regex("^\\d+\\.\\s*"), "").trim()
        if (category.equals(cleanCat, ignoreCase = true)) return true
        return categories.any { it.equals(cleanCat, ignoreCase = true) }
    }
}

data class MelodyNote(
    val pitchHz: Float,
    val durationMs: Long
)

data class CallToWorshipItem(
    val number: Int,
    val text: String,
    val scripture: String
)

data class TopicalIndexItem(
    val number: Int,
    val topicName: String,
    val hymnRange: String,
    val hymnCount: Int = 0
)

enum class ReadingTheme(val displayName: String) {
    PARCHMENT("Parchment Ivory"),
    NAVY("Midnight Navy"),
    FOREST("Sacred Forest"),
    BURGUNDY("Chancel Burgundy"),
    DARK("Candlelight Dark")
}

enum class HymnCategory(
    val number: Int,
    val displayName: String,
    val hymnRange: String,
    val hymnIds: List<Int>
) {
    ALL(0, "All", "1-520", (1..520).toList()),
    PRAISE(1, "Praise & Adoration", "1-22, 330", (1..22).toList() + listOf(330)),
    WORSHIP(2, "Worship", "23-53", (23..53).toList()),
    PRAYER(3, "Prayer & Consecration", "54-72, 440", (54..72).toList() + listOf(440)),
    FAITH(4, "Faith & Trust", "73-97, 50", (73..97).toList() + listOf(50)),
    GOSPEL(5, "The Gospel", "98-116", (98..116).toList()),
    INVITATION(6, "Gospel Invitation", "117-140, 214", (117..140).toList() + listOf(214)),
    LOVE(7, "Love", "141-158, 83", (141..158).toList() + listOf(83)),
    ASSURANCE(8, "Assurance & Testimony", "159-179, 24, 107, 331, 404, 406, 407", (159..179).toList() + listOf(24, 107, 331, 404, 406, 407)),
    GUIDANCE(9, "Guidance", "180-193, 51", (180..193).toList() + listOf(51)),
    MISSION(10, "Mission & Evangelism", "194-237, 114", (194..237).toList() + listOf(114)),
    HEAVENLY_HOME(11, "Heavenly Home", "238-267", (238..267).toList()),
    REPENTANCE(12, "Repentance, Commitment & Hope", "268-291", (268..291).toList()),
    SECOND_COMING(13, "Second Coming & Rapture", "292-305", (292..305).toList()),
    FUNERAL(14, "Funeral, Life to Come", "306-325, 110, 242", (306..325).toList() + listOf(110, 242)),
    BAPTISM(15, "Baptism", "326-329", (326..329).toList()),
    CHRISTIAN_ENDEAVOUR(16, "Christian Endeavour", "330-358, 36, 213, 216, 219, 221, 233, 277", (330..358).toList() + listOf(36, 213, 216, 219, 221, 233, 277)),
    CHRISTMAS(17, "Christmas", "359-375", (359..375).toList()),
    CLOSING_WORSHIP(18, "Closing Worship", "306, 376", listOf(306, 376)),
    NEW_YEAR(19, "New Year", "377-381", (377..381).toList()),
    PALM_SUNDAY(20, "Palm Sunday", "382", listOf(382)),
    GOOD_FRIDAY_EASTER(21, "Good Friday & Easter Sunday", "383-405", (383..405).toList()),
    LORDS_SUPPER(22, "Lord’s Supper", "406-413", (406..413).toList()),
    CHRISTIAN_EXPERIENCE(23, "Christian Experience", "414-430", (414..430).toList()),
    WEDDING(24, "Wedding", "431-433", (431..433).toList()),
    DEDICATION(25, "Dedication & Church Foundation Day", "21, 195, 269, 275", listOf(21, 195, 269, 275)),
    MOTHERS_FATHERS_DAY(26, "Mothers’ & Fathers’ Day", "434-439", (434..439).toList()),
    COURAGE(27, "Conflict & Courage", "440-447, 220, 223", (440..447).toList() + listOf(220, 223)),
    JUBILEE(28, "Jubilee", "446, 447", listOf(446, 447)),
    OFFERATORY(29, "Offeratory", "448-451", (448..451).toList()),
    REVIVAL_CHORUSES(30, "Revival, Choruses & Social", "452-520", (452..520).toList());

    companion object {
        fun fromDisplayName(name: String): HymnCategory? {
            val clean = name.replace(Regex("^\\d+\\.\\s*"), "").trim()
            return entries.find { it.displayName.equals(clean, ignoreCase = true) }
        }

        fun topics(): List<HymnCategory> = entries.filter { it != ALL }
    }
}
