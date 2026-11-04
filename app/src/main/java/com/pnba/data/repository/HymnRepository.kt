package com.pnba.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pnba.data.model.Hymn
import com.pnba.data.model.ReadingTheme
import com.pnba.data.model.TopicalIndexItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import kotlin.math.min

class HymnRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sanctuary_hymnal_prefs", Context.MODE_PRIVATE)

    init {
        // Ensure complete catalog is loaded from assets
        HymnCatalog.loadFromAssets(context)
    }

    private val _favoriteIds = MutableStateFlow<Set<Int>>(loadFavorites())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private val _recentIds = MutableStateFlow<List<Int>>(loadRecents())
    val recentIds: StateFlow<List<Int>> = _recentIds.asStateFlow()

    private val _notesMap = MutableStateFlow<Map<Int, String>>(loadNotes())
    val notesMap: StateFlow<Map<Int, String>> = _notesMap.asStateFlow()

    private val _readingTheme = MutableStateFlow(loadReadingTheme())
    val readingTheme: StateFlow<ReadingTheme> = _readingTheme.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(prefs.getInt("pref_font_size", 18))
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _useSerifFont = MutableStateFlow(prefs.getBoolean("pref_use_serif", true))
    val useSerifFont: StateFlow<Boolean> = _useSerifFont.asStateFlow()

    private val _keepScreenAwake = MutableStateFlow(prefs.getBoolean("pref_keep_awake", false))
    val keepScreenAwake: StateFlow<Boolean> = _keepScreenAwake.asStateFlow()

    fun getAllHymns(): List<Hymn> = HymnCatalog.loadFromAssets(context)

    fun getHymnById(id: Int): Hymn? = HymnCatalog.loadFromAssets(context).find { it.id == id }

    fun getHymnOfTheDay(): Hymn {
        val list = getAllHymns()
        if (list.isEmpty()) return HymnCatalog.hymns.first()
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % list.size
        return list[index]
    }

    fun getSeasonalHymns(): List<Hymn> {
        val all = getAllHymns()
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) // 0 = Jan, 11 = Dec
        return when (month) {
            11 -> all.filter { it.belongsToCategory("Christmas") }.ifEmpty { all.take(6) }.take(6)
            0 -> all.filter { it.belongsToCategory("New Year") || it.belongsToCategory("Praise & Adoration") }.take(6)
            2, 3 -> all.filter { it.belongsToCategory("Good Friday & Easter Sunday") || it.belongsToCategory("The Gospel") }.take(6)
            else -> all.filter { it.belongsToCategory("Worship") || it.belongsToCategory("Praise & Adoration") }.take(6)
        }
    }

    // --- Favorites ---
    fun toggleFavorite(id: Int) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _favoriteIds.value = current
        saveFavorites(current)
    }

    fun isFavorite(id: Int): Boolean = _favoriteIds.value.contains(id)

    private fun loadFavorites(): Set<Int> {
        val raw = prefs.getString("favorites_set", "") ?: ""
        if (raw.isBlank()) return setOf(1, 2, 51, 100) // Default favorites
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    private fun saveFavorites(set: Set<Int>) {
        prefs.edit().putString("favorites_set", set.joinToString(",")).apply()
    }

    // --- Recents ---
    fun recordRecent(id: Int) {
        val current = _recentIds.value.filter { it != id }.toMutableList()
        current.add(0, id)
        val trimmed = current.take(20)
        _recentIds.value = trimmed
        prefs.edit().putString("recent_list", trimmed.joinToString(",")).apply()
    }

    private fun loadRecents(): List<Int> {
        val raw = prefs.getString("recent_list", "") ?: ""
        if (raw.isBlank()) return listOf(1, 2, 3)
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .take(20)
    }

    // --- Choir / Personal Notes ---
    fun saveNote(hymnId: Int, note: String) {
        val current = _notesMap.value.toMutableMap()
        if (note.isBlank()) {
            current.remove(hymnId)
        } else {
            current[hymnId] = note.trim()
        }
        _notesMap.value = current
        val serialized = current.entries.joinToString(";;;") { "${it.key}:::${it.value}" }
        prefs.edit().putString("notes_map", serialized).apply()
    }

    fun getNote(hymnId: Int): String = _notesMap.value[hymnId] ?: ""

    private fun loadNotes(): Map<Int, String> {
        val raw = prefs.getString("notes_map", "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split(";;;").mapNotNull { entry ->
            val parts = entry.split(":::", limit = 2)
            if (parts.size == 2) {
                parts[0].toIntOrNull()?.let { id -> id to parts[1] }
            } else null
        }.toMap()
    }

    // --- Reader Settings ---
    fun setReadingTheme(theme: ReadingTheme) {
        _readingTheme.value = theme
        prefs.edit().putString("pref_reading_theme", theme.name).apply()
    }

    private fun loadReadingTheme(): ReadingTheme {
        val name = prefs.getString("pref_reading_theme", ReadingTheme.PARCHMENT.name) ?: ReadingTheme.PARCHMENT.name
        return try {
            ReadingTheme.valueOf(name)
        } catch (e: Exception) {
            ReadingTheme.PARCHMENT
        }
    }

    fun setFontSizeSp(sizeSp: Int) {
        val clamped = sizeSp.coerceIn(14, 32)
        _fontSizeSp.value = clamped
        prefs.edit().putInt("pref_font_size", clamped).apply()
    }

    fun setUseSerifFont(useSerif: Boolean) {
        _useSerifFont.value = useSerif
        prefs.edit().putBoolean("pref_use_serif", useSerif).apply()
    }

    fun setKeepScreenAwake(keepAwake: Boolean) {
        _keepScreenAwake.value = keepAwake
        prefs.edit().putBoolean("pref_keep_awake", keepAwake).apply()
    }

    fun getTopicalIndex(): List<TopicalIndexItem> = HymnCatalog.getTopicalIndex()

    fun getHymnsForCategory(category: String): List<Hymn> {
        if (category.isBlank() || category.equals("All", ignoreCase = true) || category.equals("All Categories", ignoreCase = true)) {
            return getAllHymns()
        }
        return getAllHymns().filter { it.belongsToCategory(category) }
    }

    // --- Deep Unified Search & Fuzzy Typos Matching ---
    data class SearchResult(
        val hymn: Hymn,
        val matchType: String,
        val snippet: String,
        val relevanceScore: Int
    )

    fun search(query: String, categoryFilter: String? = null): List<SearchResult> {
        val cleanQuery = query.trim().lowercase()
        val allHymns = getAllHymns().filter { hymn ->
            categoryFilter == null || hymn.belongsToCategory(categoryFilter)
        }

        if (cleanQuery.isBlank()) {
            return allHymns.map { hymn ->
                SearchResult(
                    hymn = hymn,
                    matchType = "Hymn #${hymn.id}",
                    snippet = if (hymn.englishTitle.isNotBlank()) "${hymn.englishTitle} • ${hymn.firstLine}" else hymn.firstLine,
                    relevanceScore = 1000 - hymn.id
                )
            }
        }

        val results = mutableListOf<SearchResult>()

        // 1. Direct number check
        val numberQuery = cleanQuery.toIntOrNull()
        if (numberQuery != null) {
            val hymn = getHymnById(numberQuery)
            if (hymn != null && (categoryFilter == null || hymn.belongsToCategory(categoryFilter))) {
                results.add(
                    SearchResult(
                        hymn = hymn,
                        matchType = "Hymn #${hymn.id}",
                        snippet = if (hymn.englishTitle.isNotBlank()) "${hymn.englishTitle} • ${hymn.firstLine}" else hymn.firstLine,
                        relevanceScore = 2000
                    )
                )
            }
        }

        for (hymn in allHymns) {
            if (numberQuery != null && hymn.id == numberQuery) continue // Already added

            val titleLower = hymn.title.lowercase()
            val engTitleLower = hymn.englishTitle.lowercase()
            val firstLineLower = hymn.firstLine.lowercase()
            val authorLower = hymn.author.lowercase()
            val composerLower = hymn.composer.lowercase()
            val tuneLower = hymn.tune.lowercase()
            val refLower = hymn.hymnBookRef.lowercase()
            val categoryLower = hymn.category.lowercase()
            val scriptureLower = hymn.scripture.lowercase()

            var matched = false
            var snippet = hymn.firstLine
            var matchType = "Title"
            var score = 0

            // Exact substring matches
            if (titleLower.contains(cleanQuery)) {
                matched = true
                matchType = "Poula Title"
                snippet = hymn.title
                score = 600 + if (titleLower.startsWith(cleanQuery)) 100 else 0
            } else if (engTitleLower.contains(cleanQuery)) {
                matched = true
                matchType = "English Title"
                snippet = hymn.englishTitle
                score = 550 + if (engTitleLower.startsWith(cleanQuery)) 100 else 0
            } else if (firstLineLower.contains(cleanQuery)) {
                matched = true
                matchType = "First Line"
                snippet = hymn.firstLine
                score = 450
            } else if (refLower.contains(cleanQuery) || tuneLower.contains(cleanQuery)) {
                matched = true
                matchType = "Hymnal Ref (${hymn.hymnBookRef.ifBlank { hymn.tune }})"
                snippet = "Meter: ${hymn.meter} • Key: ${hymn.keySignature}"
                score = 350
            } else if (authorLower.contains(cleanQuery) || composerLower.contains(cleanQuery)) {
                matched = true
                matchType = "Author / Composer"
                snippet = "${hymn.author} ${if (hymn.composer.isNotBlank()) "• Composer: " + hymn.composer else ""}".trim()
                score = 300
            } else if (categoryLower.contains(cleanQuery)) {
                matched = true
                matchType = "Theological Theme"
                snippet = hymn.category
                score = 250
            } else if (scriptureLower.contains(cleanQuery)) {
                matched = true
                matchType = "Scripture"
                snippet = hymn.scripture
                score = 220
            } else {
                // Verse line search
                for (verse in hymn.verses) {
                    for (line in verse.lines) {
                        if (line.lowercase().contains(cleanQuery)) {
                            matched = true
                            matchType = "Verse ${verse.verseNumber}"
                            snippet = line
                            score = 180
                            break
                        }
                    }
                    if (matched) break
                }
                // Chorus search
                if (!matched && hymn.chorus != null) {
                    for (line in hymn.chorus) {
                        if (line.lowercase().contains(cleanQuery)) {
                            matched = true
                            matchType = "Refrain / Chorus"
                            snippet = line
                            score = 170
                            break
                        }
                    }
                }
            }

            // Fuzzy matching if not yet matched (e.g. typos in title or english title)
            if (!matched && cleanQuery.length >= 3) {
                val queryWords = cleanQuery.split(" ").filter { it.length >= 3 }
                val targetWords = (titleLower + " " + engTitleLower).split(" ", ",", "!", "?", ".", "-")

                var fuzzyScore = 0
                for (qWord in queryWords) {
                    var bestDistance = 99
                    for (tWord in targetWords) {
                        if (tWord.length >= 3) {
                            val dist = levenshteinDistance(qWord, tWord)
                            if (dist < bestDistance) bestDistance = dist
                        }
                    }
                    if (bestDistance <= 2) {
                        fuzzyScore += (12 - bestDistance * 4)
                    }
                }

                if (fuzzyScore > 0) {
                    matched = true
                    matchType = "Fuzzy Match (~${hymn.title})"
                    snippet = if (hymn.englishTitle.isNotBlank()) "${hymn.title} (${hymn.englishTitle})" else hymn.title
                    score = 120 + fuzzyScore
                }
            }

            if (matched) {
                results.add(
                    SearchResult(
                        hymn = hymn,
                        matchType = matchType,
                        snippet = snippet,
                        relevanceScore = score
                    )
                )
            }
        }

        return results.sortedByDescending { it.relevanceScore }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1, // deletion
                    min(
                        dp[i][j - 1] + 1, // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                )
            }
        }
        return dp[m][n]
    }
}
