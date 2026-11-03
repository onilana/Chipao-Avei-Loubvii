package com.pnba.data.repository

import android.content.Context
import com.pnba.data.model.Hymn
import com.pnba.data.model.HymnCategory
import com.pnba.data.model.HymnVerse
import com.pnba.data.model.TopicalIndexItem
import org.json.JSONArray
import java.io.InputStream

object HymnCatalog {
    @Volatile
    private var _cachedHymns: List<Hymn>? = null

    val hymns: List<Hymn>
        get() = _cachedHymns ?: fallbackHymns

    val maxHymnNumber: Int
        get() = if (hymns.isNotEmpty()) hymns.maxOf { it.id } else 500

    fun totalCount(): Int = hymns.size

    fun getById(id: Int): Hymn? = hymns.find { it.id == id }

    fun getTopicalIndex(): List<TopicalIndexItem> =
        HymnCategory.topics().map { cat ->
            val count = hymns.count { it.belongsToCategory(cat.displayName) }
            TopicalIndexItem(
                number = cat.number,
                topicName = cat.displayName,
                hymnRange = cat.hymnRange,
                hymnCount = count
            )
        }

    @Synchronized
    fun loadFromAssets(context: Context): List<Hymn> {
        val existing = _cachedHymns
        if (existing != null && existing.size >= 520) {
            return existing
        }
        try {
            val jsonStream: InputStream = context.assets.open("hymns.json")
            val jsonString = jsonStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = ArrayList<Hymn>(jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val title = obj.getString("title")
                val englishTitle = obj.optString("englishTitle", "")
                val firstLine = obj.optString("firstLine", "")
                val timeSignature = obj.optString("timeSignature", "")
                val keySignature = obj.optString("keySignature", "")
                val hymnBookRef = obj.optString("hymnBookRef", "")
                val author = obj.optString("author", "")
                val composer = obj.optString("composer", "")
                val scripture = obj.optString("scripture", "")
                val category = obj.optString("category", "Worship")
                val categoriesArr = obj.optJSONArray("categories")
                val categories = mutableListOf<String>()
                if (categoriesArr != null) {
                    for (c in 0 until categoriesArr.length()) {
                        categories.add(categoriesArr.getString(c))
                    }
                }
                if (categories.isEmpty()) {
                    categories.add(category)
                }
                val keynotePitchHz = obj.optDouble("keynotePitchHz", 440.0).toFloat()
                val keynoteName = obj.optString("keynoteName", "A")
                val reflection = obj.optString("reflection", "")
                val tune = obj.optString("tune", if (englishTitle.isNotBlank()) englishTitle else hymnBookRef)
                val meter = obj.optString("meter", timeSignature)
                val year = obj.optString("year", "")

                val versesArray = obj.optJSONArray("verses")
                val verses = mutableListOf<HymnVerse>()
                if (versesArray != null) {
                    for (v in 0 until versesArray.length()) {
                        val vObj = versesArray.getJSONObject(v)
                        val vNum = vObj.getInt("verseNumber")
                        val linesArr = vObj.getJSONArray("lines")
                        val lines = mutableListOf<String>()
                        for (l in 0 until linesArr.length()) {
                            lines.add(linesArr.getString(l))
                        }
                        verses.add(HymnVerse(vNum, lines))
                    }
                }

                val chorusArray = obj.optJSONArray("chorus")
                val chorus = if (chorusArray != null) {
                    val cLines = mutableListOf<String>()
                    for (c in 0 until chorusArray.length()) {
                        cLines.add(chorusArray.getString(c))
                    }
                    cLines
                } else null

                list.add(
                    Hymn(
                        id = id,
                        title = title,
                        englishTitle = englishTitle,
                        firstLine = firstLine,
                        timeSignature = timeSignature,
                        keySignature = keySignature,
                        hymnBookRef = hymnBookRef,
                        author = author,
                        composer = composer,
                        scripture = scripture,
                        category = category,
                        categories = categories,
                        verses = verses,
                        chorus = chorus,
                        keynotePitchHz = keynotePitchHz,
                        keynoteName = keynoteName,
                        reflection = reflection,
                        tune = tune,
                        meter = meter,
                        year = year
                    )
                )
            }
            _cachedHymns = list
            return list
        } catch (e: Exception) {
            e.printStackTrace()
            return _cachedHymns ?: fallbackHymns
        }
    }

    // Fallback hymns before context initialization
    private val fallbackHymns: List<Hymn> = listOf(
        Hymn(
            id = 1,
            title = "O VEO THO LAIKHE",
            englishTitle = "O Worship The King",
            firstLine = "O, Veo tho laikhe hrülai jü veipu,",
            timeSignature = "3/4",
            keySignature = "Doh is A",
            hymnBookRef = "C.W.H. 94",
            author = "Robert Great",
            composer = "J. Michael Hayden",
            scripture = "Ps. 29:2",
            category = "Praise & Adoration",
            keynotePitchHz = 440.0f,
            keynoteName = "A",
            verses = listOf(
                HymnVerse(1, listOf(
                    "O, Veo tho laikhe hrülai jü veipu,",
                    "Puh vei khupa leshi lou soulaikhe",
                    "Etao vei rü alia vupeiyu Mapao,",
                    "Varuyu ki hinou Puhye paosho bue."
                )),
                HymnVerse(2, listOf(
                    "Puh nou khopeiyu, thaonou paolou süe?",
                    "Teikhai hinou süe, teiva hinou süe;",
                    "Deichi hi reitoutao, deikho hi kükoue,",
                    "Veishoyu zidzü nou kükou soushee peie."
                ))
            )
        )
    )
}
