package com.ttop.app.apex.util

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.content.res.use
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.ttop.app.apex.*
import com.ttop.app.apex.extensions.getIntRes
import com.ttop.app.apex.extensions.getStringOrDefault
import com.ttop.app.apex.helper.SortOrder.*
import com.ttop.app.apex.model.CategoryInfo
import com.ttop.app.apex.transform.*
import com.ttop.app.apex.ui.fragments.AlbumCoverStyle
import com.ttop.app.apex.ui.fragments.GridStyle
import com.ttop.app.apex.ui.fragments.NowPlayingScreen
import com.ttop.app.apex.ui.fragments.folder.FoldersFragment
import com.ttop.app.apex.util.theme.ThemeMode
import java.io.File


object PreferenceUtil {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext())

    val defaultCategories = listOf(
        CategoryInfo(CategoryInfo.Category.Home, true),
        CategoryInfo(CategoryInfo.Category.Songs, true),
        CategoryInfo(CategoryInfo.Category.Albums, true),
        CategoryInfo(CategoryInfo.Category.Artists, true),
        CategoryInfo(CategoryInfo.Category.Playlists, false),
        CategoryInfo(CategoryInfo.Category.Genres, false),
        CategoryInfo(CategoryInfo.Category.Folder, true),
        CategoryInfo(CategoryInfo.Category.Search, false),
        CategoryInfo(CategoryInfo.Category.Settings,false)
    )

    var libraryCategory: List<CategoryInfo>
        get() {
            val gson = Gson()
            val collectionType = object : TypeToken<List<CategoryInfo>>() {}.type
            val data = sharedPreferences.getStringOrDefault(
                LIBRARY_CATEGORIES,
                gson.toJson(defaultCategories, collectionType)
            )
            return try {
                Gson().fromJson(data, collectionType)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                return defaultCategories
            }
        }
        set(value) {
            val collectionType = object : TypeToken<List<CategoryInfo?>?>() {}.type
            sharedPreferences.edit {
                putString(LIBRARY_CATEGORIES, Gson().toJson(value, collectionType))
            }
        }

    fun registerOnSharedPreferenceChangedListener(
        listener: OnSharedPreferenceChangeListener,
    ) = sharedPreferences.registerOnSharedPreferenceChangeListener(listener)


    fun unregisterOnSharedPreferenceChangedListener(
        changeListener: OnSharedPreferenceChangeListener,
    ) = sharedPreferences.unregisterOnSharedPreferenceChangeListener(changeListener)


    val baseTheme get() = sharedPreferences.getString(GENERAL_THEME, "auto")

    fun getGeneralThemeValue(isSystemDark: Boolean): ThemeMode {
        val themeMode: String =
            sharedPreferences.getStringOrDefault(GENERAL_THEME, "auto")
        return if (isBlackMode && isSystemDark && themeMode != "light") {
            ThemeMode.BLACK
        } else {
            if (isBlackMode && themeMode == "dark") {
                ThemeMode.BLACK
            } else {
                when (themeMode) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    "auto" -> ThemeMode.AUTO
                    else -> ThemeMode.AUTO
                }
            }
        }
    }

    var languageCode: String
        get() = sharedPreferences.getString(LANGUAGE_NAME, "auto") ?: "auto"
        set(value) = sharedPreferences.edit {
            putString(LANGUAGE_NAME, value)
        }

    var isLocaleAutoStorageEnabled: Boolean
        get() = sharedPreferences.getBoolean(
            LOCALE_AUTO_STORE_ENABLED,
            false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(LOCALE_AUTO_STORE_ENABLED, value)
        }

    private val autoDownloadImagesPolicy
        get() = sharedPreferences.getStringOrDefault(
            AUTO_DOWNLOAD_IMAGES_POLICY,
            "only_wifi"
        )

    var albumArtistsOnly
        get() = sharedPreferences.getBoolean(
            ALBUM_ARTISTS_ONLY,
            false
        )
        set(value) = sharedPreferences.edit { putBoolean(ALBUM_ARTISTS_ONLY, value) }

    var albumDetailSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_DETAIL_SONG_SORT_ORDER,
            AlbumSongSortOrder.SONG_TRACK_LIST
        )
        set(value) = sharedPreferences.edit { putString(ALBUM_DETAIL_SONG_SORT_ORDER, value) }

    var artistDetailSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_DETAIL_SONG_SORT_ORDER,
            ArtistSongSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit { putString(ARTIST_DETAIL_SONG_SORT_ORDER, value) }

    var songSortOrder
        get() = sharedPreferences.getStringOrDefault(
            SONG_SORT_ORDER,
            SongSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(SONG_SORT_ORDER, value)
        }

    var albumSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_SORT_ORDER,
            AlbumSortOrder.ALBUM_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(ALBUM_SORT_ORDER, value)
        }


    var artistSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_SORT_ORDER,
            ArtistSortOrder.ARTIST_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(ARTIST_SORT_ORDER, value)
        }

    val albumSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ALBUM_SONG_SORT_ORDER,
            AlbumSongSortOrder.SONG_TRACK_LIST
        )

    val artistSongSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_SONG_SORT_ORDER,
            AlbumSongSortOrder.SONG_TRACK_LIST
        )

    val artistAlbumSortOrder
        get() = sharedPreferences.getStringOrDefault(
            ARTIST_ALBUM_SORT_ORDER,
            ArtistAlbumSortOrder.ALBUM_A_Z
        )

    var playlistSortOrder
        get() = sharedPreferences.getStringOrDefault(
            PLAYLIST_SORT_ORDER,
            PlaylistSortOrder.PLAYLIST_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(PLAYLIST_SORT_ORDER, value)
        }

    val genreSortOrder
        get() = sharedPreferences.getStringOrDefault(
            GENRE_SORT_ORDER,
            GenreSortOrder.GENRE_A_Z
        )

    val isIgnoreMediaStoreArtwork
        get() = sharedPreferences.getBoolean(
            IGNORE_MEDIA_STORE_ARTWORK,
            false
        )

    var isInitializedBlacklist
        get() = sharedPreferences.getBoolean(
            INITIALIZED_BLACKLIST, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(INITIALIZED_BLACKLIST, value)
        }

    val isBlackMode
        get() = sharedPreferences.getBoolean(
            BLACK_THEME, false
        )

    val isExtraControls
        get() = sharedPreferences.getBoolean(
            TOGGLE_ADD_CONTROLS, false
        )

    val isScreenOnEnabled get() = sharedPreferences.getBoolean(KEEP_SCREEN_ON, false)

    val isPauseOnZeroVolume get() = sharedPreferences.getBoolean(PAUSE_ON_ZERO_VOLUME, true)

    var isSleepTimerFinishMusic
        get() = sharedPreferences.getBoolean(
            SLEEP_TIMER_FINISH_SONG, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(SLEEP_TIMER_FINISH_SONG, value)
        }

    val isHeadsetPlugged
        get() = sharedPreferences.getBoolean(
            TOGGLE_HEADSET, false
        )

    var isBluetoothSpeaker
        get() = sharedPreferences.getBoolean(
            BLUETOOTH_PLAYBACK, false
        )

        set(value) = sharedPreferences.edit {
            putBoolean(BLUETOOTH_PLAYBACK, value)
        }

    val blurAmount get() = sharedPreferences.getInt(NEW_BLUR_AMOUNT, 25)

    val isCarouselEffect
        get() = sharedPreferences.getBoolean(
            CAROUSEL_EFFECT, false
        )

    var isDesaturatedColor
        get() = sharedPreferences.getBoolean(
            DESATURATED_COLOR, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(DESATURATED_COLOR, value)
        }

    val isGapLessPlayback
        get() = sharedPreferences.getBoolean(
            GAP_LESS_PLAYBACK, false
        )

    val isAdaptiveColor
        get() = sharedPreferences.getBoolean(
            ADAPTIVE_COLOR_APP, false
        )

    val isFullScreenMode
        get() = sharedPreferences.getBoolean(
            TOGGLE_FULL_SCREEN, false
        )

    val isAudioFocusEnabled
        get() = sharedPreferences.getBoolean(
            MANAGE_AUDIO_FOCUS, false
        )

    fun isAllowedToDownloadMetadata(context: Context): Boolean {
        return when (autoDownloadImagesPolicy) {
            "always" -> true
            "only_wifi" -> {
                val connectivityManager = context.getSystemService<ConnectivityManager>()
                val network = connectivityManager?.activeNetwork
                val capabilities = connectivityManager?.getNetworkCapabilities(network)

                capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
            "never" -> false
            else -> false
        }
    }

    var songGridStyle: GridStyle
        get() {
            val id: Int = sharedPreferences.getInt(SONG_GRID_STYLE, 3)
            // We can directly use "first" kotlin extension function here but
            // there maybe layout id stored in this so to avoid a crash we use
            // "firstOrNull"
            return GridStyle.values().firstOrNull { gridStyle ->
                gridStyle.id == id
            } ?: GridStyle.Circular
        }
        set(value) = sharedPreferences.edit {
            putInt(SONG_GRID_STYLE, value.id)
        }

    var albumGridStyle: GridStyle
        get() {
            val id: Int = sharedPreferences.getInt(ALBUM_GRID_STYLE, 3)
            return GridStyle.values().firstOrNull { gridStyle ->
                gridStyle.id == id
            } ?: GridStyle.Circular
        }
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_STYLE, value.id)
        }

    var artistGridStyle: GridStyle
        get() {
            val id: Int = sharedPreferences.getInt(ARTIST_GRID_STYLE, 3)
            return GridStyle.values().firstOrNull { gridStyle ->
                gridStyle.id == id
            } ?: GridStyle.Circular
        }
        set(value) = sharedPreferences.edit {
            putInt(ARTIST_GRID_STYLE, value.id)
        }

    val filterLengthMin get() = sharedPreferences.getInt(FILTER_SONG_MIN, 0)
    val filterLengthMax get() = sharedPreferences.getInt(FILTER_SONG_MAX, 10)

    var lastVersion
        // This was stored as an integer before now it's a long, so avoid a ClassCastException
        get() = try {
            sharedPreferences.getLong(LAST_CHANGELOG_VERSION, 0)
        } catch (e: ClassCastException) {
            sharedPreferences.edit { remove(LAST_CHANGELOG_VERSION) }
            0
        }
        set(value) = sharedPreferences.edit {
            putLong(LAST_CHANGELOG_VERSION, value)
        }

    var lastSleepTimerValue
        get() = sharedPreferences.getInt(
            LAST_SLEEP_TIMER_VALUE,
            30
        )
        set(value) = sharedPreferences.edit {
            putInt(LAST_SLEEP_TIMER_VALUE, value)
        }


    var nextSleepTimerElapsedRealTime
        get() = sharedPreferences.getInt(
            NEXT_SLEEP_TIMER_ELAPSED_REALTIME,
            -1
        )
        set(value) = sharedPreferences.edit {
            putInt(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, value)
        }

    fun themeResFromPrefValue(themePrefValue: String): Int {
        return when (themePrefValue) {
            "light" -> R.style.Theme_Apex_Light
            "dark" -> R.style.Theme_Apex
            else -> R.style.Theme_Apex
        }
    }

    val homeArtistGridStyle: Int
        get() {
            val position = sharedPreferences.getStringOrDefault(
                HOME_ARTIST_GRID_STYLE, "0"
            ).toInt()
            val layoutRes =
                App.getContext().resources.obtainTypedArray(R.array.pref_home_grid_style_layout)
                    .use {
                        it.getResourceId(position, 0)
                    }
            return if (layoutRes == 0) {
                R.layout.item_artist
            } else layoutRes
        }

    val homeAlbumGridStyle: Int
        get() {
            val position = sharedPreferences.getStringOrDefault(
                HOME_ALBUM_GRID_STYLE, "0"
            ).toInt()
            val layoutRes = App.getContext()
                .resources.obtainTypedArray(R.array.pref_home_grid_style_layout).use {
                    it.getResourceId(position, 0)
                }
            return if (layoutRes == 0) {
                R.layout.item_image
            } else layoutRes
        }

    val tabTitleMode: Int
        get() {
            return when (sharedPreferences.getStringOrDefault(
                TAB_TEXT_MODE, "0"
            ).toInt()) {
                0 -> BottomNavigationView.LABEL_VISIBILITY_AUTO
                1 -> BottomNavigationView.LABEL_VISIBILITY_LABELED
                2 -> BottomNavigationView.LABEL_VISIBILITY_SELECTED
                3 -> BottomNavigationView.LABEL_VISIBILITY_UNLABELED
                else -> BottomNavigationView.LABEL_VISIBILITY_LABELED
            }
        }


    var songGridSize
        get() = sharedPreferences.getInt(
            SONG_GRID_SIZE,
            App.getContext().getIntRes(R.integer.default_list_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(SONG_GRID_SIZE, value)
        }

    var songGridSizeLand
        get() = sharedPreferences.getInt(
            SONG_GRID_SIZE_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(SONG_GRID_SIZE_LAND, value)
        }


    var albumGridSize: Int
        get() = sharedPreferences.getInt(
            ALBUM_GRID_SIZE,
            App.getContext().getIntRes(R.integer.default_grid_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_SIZE, value)
        }


    var albumGridSizeLand
        get() = sharedPreferences.getInt(
            ALBUM_GRID_SIZE_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_SIZE_LAND, value)
        }


    var artistGridSize
        get() = sharedPreferences.getInt(
            ARTIST_GRID_SIZE,
            App.getContext().getIntRes(R.integer.default_grid_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(ARTIST_GRID_SIZE, value)
        }


    var artistGridSizeLand
        get() = sharedPreferences.getInt(
            ARTIST_GRID_SIZE_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_SIZE_LAND, value)
        }


    var playlistGridSize
        get() = sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE,
            App.getContext().getIntRes(R.integer.default_grid_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(PLAYLIST_GRID_SIZE, value)
        }


    var playlistGridSizeLand
        get() = sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(PLAYLIST_GRID_SIZE, value)
        }

    var albumGridSizeTablet
        get() = sharedPreferences.getInt(
            ALBUM_GRID_SIZE_TABLET,
            App.getContext().getIntRes(R.integer.default_grid_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_SIZE_TABLET, value)
        }

    var albumGridSizeTabletLand
        get() = sharedPreferences.getInt(
            ALBUM_GRID_SIZE_TABLET_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(ALBUM_GRID_SIZE_TABLET_LAND, value)
        }

    var artistGridSizeTablet
        get() = sharedPreferences.getInt(
            ARTIST_GRID_SIZE_TABLET,
            App.getContext().getIntRes(R.integer.default_grid_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(ARTIST_GRID_SIZE_TABLET, value)
        }

    var artistGridSizeTabletLand
        get() = sharedPreferences.getInt(
            ARTIST_GRID_SIZE_TABLET_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(ARTIST_GRID_SIZE_TABLET_LAND, value)
        }

    var songGridSizeTablet
        get() = sharedPreferences.getInt(
            SONG_GRID_SIZE_TABLET,
            App.getContext().getIntRes(R.integer.default_list_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(SONG_GRID_SIZE_TABLET, value)
        }

    var songGridSizeTabletLand
        get() = sharedPreferences.getInt(
            SONG_GRID_SIZE_TABLET_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(SONG_GRID_SIZE_TABLET_LAND, value)
        }

    var playlistGridSizeTablet
        get() = sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE_TABLET,
            App.getContext().getIntRes(R.integer.default_grid_columns)
        )
        set(value) = sharedPreferences.edit {
            putInt(PLAYLIST_GRID_SIZE_TABLET, value)
        }

    var playlistGridSizeTabletLand
        get() = sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE_TABLET_LAND,
            App.getContext().getIntRes(R.integer.default_grid_columns_land)
        )
        set(value) = sharedPreferences.edit {
            putInt(PLAYLIST_GRID_SIZE_TABLET_LAND, value)
        }

    fun checkPreferences(setting: String?): Boolean {
        return sharedPreferences.contains(setting)
    }

    var albumCoverStyle: AlbumCoverStyle
        get() {
            val id: Int = sharedPreferences.getInt(ALBUM_COVER_STYLE, 0)
            for (albumCoverStyle in AlbumCoverStyle.values()) {
                if (albumCoverStyle.id == id) {
                    return albumCoverStyle
                }
            }
            return AlbumCoverStyle.Card
        }
        set(value) = sharedPreferences.edit { putInt(ALBUM_COVER_STYLE, value.id) }


    var nowPlayingScreen: NowPlayingScreen
        get() {
            val id: Int = sharedPreferences.getInt(NOW_PLAYING_SCREEN_ID, 3)
            for (nowPlayingScreen in NowPlayingScreen.values()) {
                if (nowPlayingScreen.id == id) {
                    return nowPlayingScreen
                }
            }
            return NowPlayingScreen.Classic
        }
        set(value) = sharedPreferences.edit {
            putInt(NOW_PLAYING_SCREEN_ID, value.id)
            // Also set a cover theme for that now playing
            value.defaultCoverTheme?.let { coverTheme -> albumCoverStyle = coverTheme }
        }

    val albumCoverTransform: ViewPager.PageTransformer
        get() {
            val style = sharedPreferences.getStringOrDefault(
                ALBUM_COVER_TRANSFORM,
                "0"
            ).toInt()
            return when (style) {
                0 -> NormalPageTransformer()
                1 -> CascadingPageTransformer()
                2 -> DepthTransformation()
                3 -> HorizontalFlipTransformation()
                4 -> VerticalFlipTransformation()
                5 -> HingeTransformation()
                6 -> VerticalStackTransformer()
                else -> ViewPager.PageTransformer { _, _ -> }
            }
        }

    var startDirectory: File
        get() {
            val folderPath = FoldersFragment.defaultStartDirectory.path
            val filePath: String = sharedPreferences.getStringOrDefault(START_DIRECTORY, folderPath)
            return File(filePath)
        }
        set(value) = sharedPreferences.edit {
            putString(
                START_DIRECTORY,
                FileUtil.safeGetCanonicalPath(value)
            )
        }

    fun getRecentlyPlayedCutoffTimeMillis(): Long {
        val calendarUtil = CalendarUtil()
        val interval: Long = when (sharedPreferences.getString(RECENTLY_PLAYED_CUTOFF, "")) {
            "today" -> calendarUtil.elapsedToday
            "this_week" -> calendarUtil.elapsedWeek
            "past_seven_days" -> calendarUtil.getElapsedDays(7)
            "past_three_months" -> calendarUtil.getElapsedMonths(3)
            "this_year" -> calendarUtil.elapsedYear
            "this_month" -> calendarUtil.elapsedMonth
            else -> calendarUtil.elapsedMonth
        }
        return System.currentTimeMillis() - interval
    }

    val lastAddedCutoff: Long
        get() {
            val calendarUtil = CalendarUtil()
            val interval =
                when (sharedPreferences.getStringOrDefault(LAST_ADDED_CUTOFF, "this_month")) {
                    "today" -> calendarUtil.elapsedToday
                    "this_week" -> calendarUtil.elapsedWeek
                    "past_three_months" -> calendarUtil.getElapsedMonths(3)
                    "this_year" -> calendarUtil.elapsedYear
                    "this_month" -> calendarUtil.elapsedMonth
                    else -> calendarUtil.elapsedMonth
                }
            return (System.currentTimeMillis() - interval) / 1000
        }

    val homeSuggestions: Boolean
        get() = sharedPreferences.getBoolean(
            TOGGLE_SUGGESTIONS,
            true
        )

    val pauseHistory: Boolean
        get() = sharedPreferences.getBoolean(
            PAUSE_HISTORY,
            false
        )

    var audioFadeDuration
        get() = sharedPreferences
            .getInt(AUDIO_FADE_DURATION, 0)
        set(value) = sharedPreferences.edit { putInt(AUDIO_FADE_DURATION, value) }

    var showLyrics: Boolean
        get() = sharedPreferences.getBoolean(SHOW_LYRICS, false)
        set(value) = sharedPreferences.edit { putBoolean(SHOW_LYRICS, value) }

    val rememberLastTab: Boolean
        get() = sharedPreferences.getBoolean(REMEMBER_LAST_TAB, true)

    var lastTab: Int
        get() = sharedPreferences
            .getInt(LAST_USED_TAB, 0)
        set(value) = sharedPreferences.edit { putInt(LAST_USED_TAB, value) }

    val isWhiteList: Boolean
        get() = sharedPreferences.getBoolean(WHITELIST_MUSIC, true)

    val crossFadeDuration
        get() = sharedPreferences
            .getInt(CROSS_FADE_DURATION, 0)

    var materialYou
        get() = sharedPreferences.getBoolean(MATERIAL_YOU, true)
        set(value) = sharedPreferences.edit { putBoolean(MATERIAL_YOU, value) }

    val isCustomFont
        get() = sharedPreferences.getString(CUSTOM_FONT, "default")

    val lyricsType: CoverLyricsType
        get() = if (sharedPreferences.getString(LYRICS_TYPE, "0") == "0") {
            CoverLyricsType.REPLACE_COVER
        } else {
            CoverLyricsType.OVER_COVER
        }

    var playbackSpeed
        get() = sharedPreferences
            .getFloat(PLAYBACK_SPEED, 1F)
        set(value) = sharedPreferences.edit { putFloat(PLAYBACK_SPEED, value) }

    var playbackPitch
        get() = sharedPreferences
            .getFloat(PLAYBACK_PITCH, 1F)
        set(value) = sharedPreferences.edit { putFloat(PLAYBACK_PITCH, value) }

    var appBarMode
        get() = sharedPreferences.getStringOrDefault(
            APPBAR_MODE, "simple"
        )
        set(value) = sharedPreferences.edit {
            putString(APPBAR_MODE, value)}


    val lyricsScreenOn
        get() = sharedPreferences.getBoolean(SCREEN_ON_LYRICS, false)

    var swipeAnywhereToChangeSong
        get() = sharedPreferences.getString(
            SWIPE_ANYWHERE_NOW_PLAYING, "off"
        )

        set(value) = sharedPreferences.edit {
            putString(SWIPE_ANYWHERE_NOW_PLAYING, value)}

    var tempValue
        get() = sharedPreferences.getInt(TEMP_VALUE, 0)
        set(value) = sharedPreferences.edit {
            putInt(TEMP_VALUE, value)
        }

    var shouldRecreate
        get() = sharedPreferences.getBoolean(
            SHOULD_RECREATE, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(SHOULD_RECREATE, value)}

    var shouldRecreateTabs
        get() = sharedPreferences.getBoolean(
            SHOULD_RECREATE_TABS, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(SHOULD_RECREATE_TABS, value)}

    val specificDevice
        get() = sharedPreferences.getBoolean(SPECIFIC_DEVICE, false)

    val bluetoothDevice
        get() = sharedPreferences.getString(BLUETOOTH_DEVICE, "")

    var isQueueHiddenPeek
        get() = sharedPreferences.getBoolean(
            IS_QUEUE_HIDDEN_PEEK, true
        )
        set(value) = sharedPreferences.edit {
            putBoolean(IS_QUEUE_HIDDEN_PEEK, value)}

    val syncedLyrics
        get() = sharedPreferences.getBoolean(
            SYNCED_LYRICS, false
        )

    var widgetBackground
        get() = sharedPreferences.getBoolean(
            WIDGET_BACKGROUND, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(WIDGET_BACKGROUND, value)}

    var progressBarStyle
        get() = sharedPreferences.getStringOrDefault(
            PROGRESS_BAR_STYLE, "circular"
        )
        set(value) = sharedPreferences.edit {
            putString(PROGRESS_BAR_STYLE, value)}


    var isSwipe
        get() = sharedPreferences.getString(
            TOGGLE_MINI_SWIPE, "off"
        )

        set(value) = sharedPreferences.edit {
            putString(TOGGLE_MINI_SWIPE, value)}

    val isAutoplay
        get() = sharedPreferences.getBoolean(
            TOGGLE_AUTOPLAY, false
        )

    var isAutoRotate
        get() = sharedPreferences.getBoolean(
            AUTO_ROTATE, false
        )

        set(value) = sharedPreferences.edit {
            putBoolean(AUTO_ROTATE, value)}

    var isEmbedMode
        get() = sharedPreferences.getBoolean(EMBED_LYRICS, false)

        set(value) = sharedPreferences.edit {
            putBoolean(EMBED_LYRICS, value)}

    var isWidgetPanel
        get() = sharedPreferences.getBoolean(WIDGET_PANEL, false)

        set(value) = sharedPreferences.edit {
            putBoolean(WIDGET_PANEL, value)}

    val isExpandPanel
        get() = sharedPreferences.getString(EXPAND_NOW_PLAYING_PANEL, "disabled")

    var isDevModeEnabled
        get() = sharedPreferences.getBoolean(DEV_MODE, false)

        set(value) = sharedPreferences.edit {
            putBoolean(DEV_MODE, value)}

    val isAction1
        get() = sharedPreferences.getStringOrDefault(NOTIFICATION_ACTION_1, "none")

    val isAction2
        get() = sharedPreferences.getStringOrDefault(NOTIFICATION_ACTION_2, "none")

    val isDisableWidgets
        get() = sharedPreferences.getBoolean(DISABLE_WIDGETS, false)

    var isInternetConnected
        get() = sharedPreferences.getBoolean(
            INTERNET_CONNECTED, true
        )

        set(value) = sharedPreferences.edit {
            putBoolean(INTERNET_CONNECTED, value)}

    var buttonColorOnWidgets
        get() = sharedPreferences.getBoolean(
            WIDGET_BUTTON_COLOR, true
        )
        set(value) = sharedPreferences.edit {
            putBoolean(WIDGET_BUTTON_COLOR, value)}

    var isTimerCancelled
        get() = sharedPreferences.getBoolean(
            "TIMER_CANCELLED", true)

        set(value) = sharedPreferences.edit {
            putBoolean("TIMER_CANCELLED", value)}

    var queueStyle
        get() = sharedPreferences.getString(
            QUEUE_STYLE, "duo"
        )

        set(value) = sharedPreferences.edit {
            putString(QUEUE_STYLE, value)}

    var queueStyleLand
        get() = sharedPreferences.getString(
            QUEUE_STYLE_LAND, "trio"
        )

        set(value) = sharedPreferences.edit {
            putString(QUEUE_STYLE, value)}

    val isPlayerBackgroundType
        get() = sharedPreferences.getBoolean(
            PLAYER_BACKGROUND, false
        )

    var backupPath
        get() = sharedPreferences.getString(
            BACKUP_PATH, getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        )

        set(value) = sharedPreferences.edit {
            putString(BACKUP_PATH, value)}

    val isDisableWidgetUpdate
        get() = sharedPreferences.getBoolean(
            DISABLE_UPDATE, false
        )
    var scrollbarStyle
        get() = sharedPreferences.getStringOrDefault(
            SCROLLBAR_STYLE, "auto_hide"
        )
        set(value) = sharedPreferences.edit {
            putString(APPBAR_MODE, value)}

    val isColorAnimate
        get() = sharedPreferences.getBoolean(
            COLOR_ANIMATE, false
        )

    var isCarConnected
        get() = sharedPreferences.getBoolean(
            CAR_CONNECTED, false)

        set(value) = sharedPreferences.edit {
            putBoolean(CAR_CONNECTED, value)}

    val isAutoAction1
        get() = sharedPreferences.getStringOrDefault(AUTO_ACTION_1, "none")

    val isAutoAction2
        get() = sharedPreferences.getStringOrDefault(AUTO_ACTION_2, "none")

    val isNotificationActionsOnAuto
        get() = sharedPreferences.getBoolean(
            USE_NOTI_ACTIONS_AUTO, true
        )

    val searchActionShuffle
        get() = sharedPreferences.getBoolean(
            SEARCH_ACTION, false
        )

    val isVoiceSearch
        get() = sharedPreferences.getBoolean(
            SEARCH_ICON_NAVIGATION, false
        )

    var isSearchFromNavigation
        get() = sharedPreferences.getBoolean(
            SEARCH_FROM_NAVIGATION, false)

        set(value) = sharedPreferences.edit {
            putBoolean(SEARCH_FROM_NAVIGATION, value)}

    val isVolumeControls
        get() = sharedPreferences.getBoolean(
            VOLUME_CONTROLS, false
        )

    val isStockEqualizer
        get() = sharedPreferences.getBoolean(
            EQUALIZER_STOCK, false
        )

    val fontSize
        get() = sharedPreferences.getString(
            FONT_SIZE, "16"
        )

    val fontSizeLyrics
        get() = sharedPreferences.getString(
            FONT_SIZE_LYRICS, "16"
        )

    val keepShuffleState
        get() = sharedPreferences.getBoolean(
            SHUFFLE_STATE, false
        )

    val rewindDuration
        get() = sharedPreferences.getInt(
        REWIND_DURATION, 10
        )

    val fastForwardDuration
        get() = sharedPreferences.getInt(
            FAST_FORWARD_DURATION, 10
        )

    val isDurationSame
        get() = sharedPreferences.getBoolean(
            DURATION_SAME, true
        )
}

enum class CoverLyricsType {
    REPLACE_COVER, OVER_COVER
}