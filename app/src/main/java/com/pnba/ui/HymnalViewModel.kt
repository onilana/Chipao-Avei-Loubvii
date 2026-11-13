package com.pnba.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pnba.data.model.Hymn
import com.pnba.data.model.ReadingTheme
import com.pnba.data.model.TopicalIndexItem
import com.pnba.data.network.Announcement
import com.pnba.data.network.AppUpdateInfo
import com.pnba.data.network.AppUpdateManager
import com.pnba.data.network.FeedbackConfig
import com.pnba.data.network.FeedbackService
import com.pnba.data.network.RemoteConfigManager
import com.pnba.data.repository.HymnCatalog
import com.pnba.data.repository.HymnRepository
import com.pnba.sound.PitchPipePlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    INDEX,
    SEARCH,
    DETAIL,
    ABOUT
}

enum class IndexViewMode {
    GRID,
    PAGE,
    TOPICAL
}

class HymnalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HymnRepository(application.applicationContext)

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedHymnId = MutableStateFlow(1)
    val selectedHymnId: StateFlow<Int> = _selectedHymnId.asStateFlow()

    // Navigation back stack for smooth back press handling
    private val backStack = mutableListOf<Screen>()

    // Quick Entry Keypad State
    private val _keypadInput = MutableStateFlow("")
    val keypadInput: StateFlow<String> = _keypadInput.asStateFlow()

    val keypadPreviewHymn: StateFlow<Hymn?> = _keypadInput.combine(MutableStateFlow(Unit)) { input, _ ->
        val num = input.toIntOrNull()
        if (num != null) repository.getHymnById(num) else null
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Index Screen State
    private val _indexMode = MutableStateFlow(IndexViewMode.GRID)
    val indexMode: StateFlow<IndexViewMode> = _indexMode.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Search Screen State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<HymnRepository.SearchResult>>(emptyList())
    val searchResults: StateFlow<List<HymnRepository.SearchResult>> = _searchResults.asStateFlow()

    // Repository states
    val favoriteIds: StateFlow<Set<Int>> = repository.favoriteIds
    val recentIds: StateFlow<List<Int>> = repository.recentIds
    val notesMap: StateFlow<Map<Int, String>> = repository.notesMap
    val readingTheme: StateFlow<ReadingTheme> = repository.readingTheme
    val fontSizeSp: StateFlow<Int> = repository.fontSizeSp
    val useSerifFont: StateFlow<Boolean> = repository.useSerifFont
    val keepScreenAwake: StateFlow<Boolean> = repository.keepScreenAwake

    // Audio / Pitch Pipe State
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _activePlayingPitch = MutableStateFlow<String?>(null)
    val activePlayingPitch: StateFlow<String?> = _activePlayingPitch.asStateFlow()

    // Feedback Dialog State
    private val _showFeedbackDialog = MutableStateFlow(false)
    val showFeedbackDialog: StateFlow<Boolean> = _showFeedbackDialog.asStateFlow()

    private val _isSubmittingFeedback = MutableStateFlow(false)
    val isSubmittingFeedback: StateFlow<Boolean> = _isSubmittingFeedback.asStateFlow()

    private val _feedbackStatusMessage = MutableStateFlow<String?>(null)
    val feedbackStatusMessage: StateFlow<String?> = _feedbackStatusMessage.asStateFlow()

    private val _feedbackErrorMessage = MutableStateFlow<String?>(null)
    val feedbackErrorMessage: StateFlow<String?> = _feedbackErrorMessage.asStateFlow()

    // App Update State
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    // Remote Config Announcement State
    private val _activeAnnouncement = MutableStateFlow<Announcement?>(null)
    val activeAnnouncement: StateFlow<Announcement?> = _activeAnnouncement.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateCheckStatus = MutableStateFlow<String?>(null)
    val updateCheckStatus: StateFlow<String?> = _updateCheckStatus.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _downloadStatusMessage = MutableStateFlow<String?>(null)
    val downloadStatusMessage: StateFlow<String?> = _downloadStatusMessage.asStateFlow()

    private val _downloadedApkFile = MutableStateFlow<java.io.File?>(null)
    val downloadedApkFile: StateFlow<java.io.File?> = _downloadedApkFile.asStateFlow()

    init {
        // Initialize search results
        updateSearch("")
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun openHymn(id: Int) {
        val hymn = repository.getHymnById(id)
        if (hymn != null) {
            _selectedHymnId.value = id
            repository.recordRecent(id)
            navigateTo(Screen.DETAIL)
        }
    }

    fun nextHymn() {
        val all = repository.getAllHymns()
        val currentIndex = all.indexOfFirst { it.id == _selectedHymnId.value }
        if (currentIndex != -1 && currentIndex < all.size - 1) {
            openHymn(all[currentIndex + 1].id)
        }
    }

    fun prevHymn() {
        val all = repository.getAllHymns()
        val currentIndex = all.indexOfFirst { it.id == _selectedHymnId.value }
        if (currentIndex > 0) {
            openHymn(all[currentIndex - 1].id)
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    // Keypad Functions
    fun appendKeypadDigit(digit: Char) {
        if (_keypadInput.value.length < 3) {
            _keypadInput.value += digit
        }
    }

    fun clearKeypad() {
        _keypadInput.value = ""
    }

    fun backspaceKeypad() {
        if (_keypadInput.value.isNotEmpty()) {
            _keypadInput.value = _keypadInput.value.dropLast(1)
        }
    }

    fun submitKeypadJump(): Boolean {
        val num = _keypadInput.value.toIntOrNull()
        if (num != null) {
            val hymn = repository.getHymnById(num)
            if (hymn != null) {
                openHymn(num)
                clearKeypad()
                return true
            }
        }
        return false
    }

    // Index Controls
    fun setIndexMode(mode: IndexViewMode) {
        _indexMode.value = mode
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    // Search Controls
    fun updateSearch(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _searchResults.value = repository.search(query)
        }
    }

    // Hymn Data Getters
    fun getAllHymns(): List<Hymn> = repository.getAllHymns()

    fun getTopicalIndex(): List<TopicalIndexItem> = repository.getTopicalIndex()

    fun getHymnsForCategory(category: String): List<Hymn> = repository.getHymnsForCategory(category)

    fun getSelectedHymn(): Hymn {
        return repository.getHymnById(_selectedHymnId.value) ?: HymnCatalog.hymns.first()
    }

    fun getHymnOfTheDay(): Hymn = repository.getHymnOfTheDay()

    fun getHymnOfTheDayOccasion(): String = repository.getHymnOfTheDayOccasion()

    fun getSeasonalHymns(): List<Hymn> = repository.getSeasonalHymns()

    fun getFavorites(): List<Hymn> {
        val ids = repository.favoriteIds.value
        return HymnCatalog.hymns.filter { ids.contains(it.id) }
    }

    fun getRecents(): List<Hymn> {
        val ids = repository.recentIds.value
        return ids.mapNotNull { repository.getHymnById(it) }
    }

    // Action toggles
    fun toggleFavorite(id: Int) = repository.toggleFavorite(id)

    fun saveNote(id: Int, note: String) = repository.saveNote(id, note)

    fun getNote(id: Int): String = repository.getNote(id)

    fun setReadingTheme(theme: ReadingTheme) = repository.setReadingTheme(theme)

    fun setFontSize(size: Int) = repository.setFontSizeSp(size)

    fun setUseSerif(useSerif: Boolean) = repository.setUseSerifFont(useSerif)

    fun setKeepScreenAwake(keep: Boolean) = repository.setKeepScreenAwake(keep)

    // Audio Playback
    fun playHymnStartingPitch(hymn: Hymn) {
        _isPlayingAudio.value = true
        _activePlayingPitch.value = hymn.keynoteName
        PitchPipePlayer.playSinglePitch(hymn.keynotePitchHz, durationMs = 2000L) {
            _isPlayingAudio.value = false
            _activePlayingPitch.value = null
        }
    }

    fun playHymnMelody(hymn: Hymn) {
        if (hymn.melodyNotes.isEmpty()) {
            playHymnStartingPitch(hymn)
            return
        }
        _isPlayingAudio.value = true
        _activePlayingPitch.value = "Melody"
        PitchPipePlayer.playMelody(hymn.melodyNotes) {
            _isPlayingAudio.value = false
            _activePlayingPitch.value = null
        }
    }

    fun playCustomPitch(noteName: String, frequencyHz: Float) {
        _isPlayingAudio.value = true
        _activePlayingPitch.value = noteName
        PitchPipePlayer.playSinglePitch(frequencyHz, durationMs = 1800L) {
            _isPlayingAudio.value = false
            _activePlayingPitch.value = null
        }
    }

    fun stopAudio() {
        PitchPipePlayer.stop()
        _isPlayingAudio.value = false
        _activePlayingPitch.value = null
    }

    // Feedback Modal Actions
    fun setFeedbackDialogVisible(visible: Boolean) {
        _showFeedbackDialog.value = visible
        if (!visible) {
            _feedbackStatusMessage.value = null
            _feedbackErrorMessage.value = null
            _isSubmittingFeedback.value = false
        }
    }

    fun submitFeedback(
        hymnNumber: String,
        typoDetail: String,
        senderName: String,
        senderEmail: String,
        onFallbackEmail: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSubmittingFeedback.value = true
            _feedbackErrorMessage.value = null
            _feedbackStatusMessage.value = null

            val result = FeedbackService.sendFeedback(
                hymnNumber = hymnNumber,
                typoDetail = typoDetail,
                senderName = senderName,
                senderEmail = senderEmail
            )

            _isSubmittingFeedback.value = false

            if (result.isSuccess) {
                val target = if (hymnNumber.isNotBlank()) "Hymn #$hymnNumber" else "the hymnal"
                _feedbackStatusMessage.value = "Thank you! Your feedback for $target has been sent directly to the editorial team."
            } else {
                val exception = result.exceptionOrNull()
                if (FeedbackConfig.WEBHOOK_URL.isBlank()) {
                    if (onFallbackEmail != null) {
                        onFallbackEmail()
                        val target = if (hymnNumber.isNotBlank()) "Hymn #$hymnNumber" else "the hymnal"
                        _feedbackStatusMessage.value = "Opening email app for $target..."
                    } else {
                        _feedbackErrorMessage.value = "Feedback Webhook URL has not been configured in FeedbackConfig.kt."
                    }
                } else {
                    _feedbackErrorMessage.value = "Failed to send: ${exception?.localizedMessage ?: "Network error"}. Please check your internet connection."
                }
            }
        }
    }

    fun checkForAppUpdates(context: Context) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateCheckStatus.value = null
            val result = AppUpdateManager.checkForUpdates(context)
            _isCheckingUpdate.value = false
            if (result.isSuccess) {
                val info = result.getOrNull()
                _updateInfo.value = info
                if (info?.isUpdateAvailable == true) {
                    _updateCheckStatus.value = "New release v${info.latestVersion} is available!"
                } else {
                    val curName = info?.currentVersion ?: AppUpdateManager.getCurrentVersionName(context)
                    _updateCheckStatus.value = "You are on the latest version (v$curName)."
                }
            } else {
                _updateCheckStatus.value = "Could not check for updates. Please check your connection."
            }
        }
    }

    fun downloadAndInstallUpdate(context: Context, downloadUrl: String) {
        val existingFile = _downloadedApkFile.value
        if (existingFile != null && existingFile.exists()) {
            AppUpdateManager.installApk(context, existingFile)
            return
        }

        viewModelScope.launch {
            _isDownloadingUpdate.value = true
            _downloadProgress.value = 0f
            _downloadStatusMessage.value = "Connecting..."

            val result = AppUpdateManager.downloadApk(
                context = context,
                downloadUrl = downloadUrl,
                onProgress = { progress, statusText ->
                    _downloadProgress.value = progress
                    _downloadStatusMessage.value = statusText
                }
            )

            _isDownloadingUpdate.value = false

            if (result.isSuccess) {
                val apkFile = result.getOrNull()
                _downloadedApkFile.value = apkFile
                _downloadStatusMessage.value = "Download complete! Opening installer..."
                if (apkFile != null) {
                    AppUpdateManager.installApk(context, apkFile)
                }
            } else {
                _downloadProgress.value = null
                _downloadStatusMessage.value = "Download failed: ${result.exceptionOrNull()?.localizedMessage ?: "Network error"}"
            }
        }
    }

    /**
     * Checks Firebase Remote Config for any active announcement and compares with
     * the last dismissed ID stored in SharedPreferences.
     */
    fun checkAnnouncements(context: Context) {
        val announcement = RemoteConfigManager.getActiveAnnouncement()
        if (announcement == null) {
            _activeAnnouncement.value = null
            return
        }
        val prefs = context.getSharedPreferences("pnba_announcements", Context.MODE_PRIVATE)
        val lastDismissedId = prefs.getString("last_dismissed_announcement_id", "")
        if (lastDismissedId != announcement.id) {
            _activeAnnouncement.value = announcement
        } else {
            _activeAnnouncement.value = null
        }
    }

    /**
     * Dismisses the current announcement popup and records its ID so it does not reappear.
     */
    fun dismissAnnouncement(context: Context, announcementId: String) {
        val prefs = context.getSharedPreferences("pnba_announcements", Context.MODE_PRIVATE)
        prefs.edit().putString("last_dismissed_announcement_id", announcementId).apply()
        _activeAnnouncement.value = null
    }

    override fun onCleared() {
        super.onCleared()
        PitchPipePlayer.stop()
    }
}
