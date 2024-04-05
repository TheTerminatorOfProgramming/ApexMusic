/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ttop.app.apex

import android.provider.BaseColumns
import android.provider.MediaStore

object Constants {
    const val RATE_ON_GOOGLE_PLAY =
        "https://play.google.com/store/apps/details?id=com.ttop.app.apex"
    const val GITHUB_PROJECT = "https://github.com/TheTerminatorOfProgramming/ApexMusic"
    const val IS_MUSIC =
        MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"

    const val DATA = "_data"

    val baseProjection = arrayOf(
        BaseColumns._ID, // 0
        MediaStore.Audio.AudioColumns.TITLE, // 1
        MediaStore.Audio.AudioColumns.TRACK, // 2
        MediaStore.Audio.AudioColumns.YEAR, // 3
        MediaStore.Audio.AudioColumns.DURATION, // 4
        DATA, // 5
        MediaStore.Audio.AudioColumns.DATE_MODIFIED, // 6
        MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
        MediaStore.Audio.AudioColumns.ALBUM, // 8
        MediaStore.Audio.AudioColumns.ARTIST_ID, // 9
        MediaStore.Audio.AudioColumns.ARTIST, // 10
        MediaStore.Audio.AudioColumns.COMPOSER, // 11
        ALBUM_ARTIST // 12
    )
    const val NUMBER_OF_TOP_TRACKS = 99
}

const val EXTRA_PLAYLIST_TYPE = "type"
const val EXTRA_GENRE = "extra_genre"
const val EXTRA_PLAYLIST = "extra_playlist"
const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
const val EXTRA_ALBUM_ID = "extra_album_id"
const val EXTRA_ARTIST_ID = "extra_artist_id"
const val EXTRA_SONG = "extra_songs"
const val EXTRA_PLAYLISTS = "extra_playlists"
const val LIBRARY_CATEGORIES = "library_categories"
const val DESATURATED_COLOR = "desaturated_color"
const val BLACK_THEME = "black_theme"
const val KEEP_SCREEN_ON = "keep_screen_on"
const val NOW_PLAYING_SCREEN_ID = "now_playing_screen_id"
const val CAROUSEL_EFFECT = "carousel_effect"
const val GAP_LESS_PLAYBACK = "gapless_playback"
const val NEW_BLUR_AMOUNT = "new_blur_amount"
const val TOGGLE_HEADSET = "toggle_headset"
const val GENERAL_THEME = "general_theme"
const val ACCENT_COLOR = "accent_color"
const val CIRCULAR_ALBUM_ART = "circular_album_art"
const val TOGGLE_FULL_SCREEN = "toggle_full_screen"
const val ADAPTIVE_COLOR_APP = "adaptive_color_app"
const val HOME_ARTIST_GRID_STYLE = "home_artist_grid_style"
const val HOME_ALBUM_GRID_STYLE = "home_album_grid_style"
const val TOGGLE_ADD_CONTROLS = "toggle_add_controls"
const val ALBUM_COVER_STYLE = "album_cover_style_id"
const val ALBUM_COVER_TRANSFORM = "album_cover_transform"
const val TAB_TEXT_MODE = "tab_text_mode"
const val LANGUAGE_NAME = "language_name"
const val SLEEP_TIMER_FINISH_SONG = "sleep_timer_finish_song"
const val ALBUM_GRID_STYLE = "album_grid_style_home"
const val ARTIST_GRID_STYLE = "artist_grid_style_home"
const val SONG_SORT_ORDER = "song_sort_order"
const val SONG_GRID_SIZE = "song_grid_size"
const val GENRE_SORT_ORDER = "genre_sort_order"
const val BLUETOOTH_PLAYBACK = "bluetooth_playback"
const val INITIALIZED_BLACKLIST = "initialized_blacklist"
const val ARTIST_SORT_ORDER = "artist_sort_order"
const val ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order"
const val ALBUM_SORT_ORDER = "album_sort_order"
const val PLAYLIST_SORT_ORDER = "playlist_sort_order"
const val ALBUM_SONG_SORT_ORDER = "album_song_sort_order"
const val ARTIST_SONG_SORT_ORDER = "artist_song_sort_order"
const val ALBUM_GRID_SIZE = "album_grid_size"
const val ALBUM_GRID_SIZE_LAND = "album_grid_size_land"
const val SONG_GRID_SIZE_LAND = "song_grid_size_land"
const val ARTIST_GRID_SIZE = "artist_grid_size"
const val ARTIST_GRID_SIZE_LAND = "artist_grid_size_land"
const val PLAYLIST_GRID_SIZE = "playlist_grid_size"
const val PLAYLIST_GRID_SIZE_LAND = "playlist_grid_size_land"
const val LAST_ADDED_CUTOFF = "last_added_interval"
const val LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value"
const val NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time"
const val IGNORE_MEDIA_STORE_ARTWORK = "ignore_media_store_artwork"
const val LAST_CHANGELOG_VERSION = "last_changelog_version"
const val AUTO_DOWNLOAD_IMAGES_POLICY = "auto_download_images_policy"
const val START_DIRECTORY = "start_directory"
const val RECENTLY_PLAYED_CUTOFF = "recently_played_interval"
const val ALBUM_ARTISTS_ONLY = "album_artists_only"
const val ALBUM_ARTIST = "album_artist"
const val ALBUM_DETAIL_SONG_SORT_ORDER = "album_detail_song_sort_order"
const val ARTIST_DETAIL_SONG_SORT_ORDER = "artist_detail_song_sort_order"
const val EQUALIZER = "equalizer"
const val SONG_GRID_STYLE = "song_grid_style"
const val PAUSE_ON_ZERO_VOLUME = "pause_on_zero_volume"
const val FILTER_SONG_MIN = "filter_song_min"
const val FILTER_SONG_MAX = "filter_song_max"
const val EXPAND_NOW_PLAYING_PANEL = "expand_now_playing_panel"
const val EXTRA_ARTIST_NAME = "extra_artist_name"
const val TOGGLE_SUGGESTIONS = "toggle_suggestions"
const val AUDIO_FADE_DURATION = "audio_fade_duration"
const val CROSS_FADE_DURATION = "cross_fade_duration"
const val SHOW_LYRICS = "show_lyrics"
const val REMEMBER_LAST_TAB = "remember_last_tab"
const val LAST_USED_TAB = "last_used_tab"
const val WHITELIST_MUSIC = "whitelist_music"
const val MATERIAL_YOU = "material_you"
const val LYRICS_TYPE = "lyrics_type"
const val PLAYBACK_SPEED = "playback_speed"
const val PLAYBACK_PITCH = "playback_pitch"
const val CUSTOM_FONT = "custom_font"
const val APPBAR_MODE = "appbar_mode"
const val SCREEN_ON_LYRICS = "screen_on_lyrics"
const val SWIPE_ANYWHERE_NOW_PLAYING = "swipe_anywhere_now_playing"
const val PAUSE_HISTORY = "pause_history"
const val MANAGE_AUDIO_FOCUS = "manage_audio_focus"
const val LOCALE_AUTO_STORE_ENABLED = "locale_auto_store_enabled"

//CUSTOM ADDED SINCE APEX 1.0.0
const val ALBUM_GRID_SIZE_TABLET = "album_grid_size_tablet"
const val ALBUM_GRID_SIZE_TABLET_LAND = "album_grid_size_tablet_land"
const val SONG_GRID_SIZE_TABLET = "song_grid_size_tablet"
const val SONG_GRID_SIZE_TABLET_LAND = "song_grid_size_tablet_land"
const val ARTIST_GRID_SIZE_TABLET = "artist_grid_size_tablet"
const val ARTIST_GRID_SIZE_TABLET_LAND = "artist_grid_size_tablet_land"
const val PLAYLIST_GRID_SIZE_TABLET = "playlist_grid_size_tablet"
const val PLAYLIST_GRID_SIZE_TABLET_LAND = "playlist_grid_size_tablet_land"
const val SPECIFIC_DEVICE = "specific_device"
const val BLUETOOTH_DEVICE = "bluetooth_device"
const val TEMP_VALUE = "temp_value"
const val SHOULD_RECREATE = "should_recreate"
const val SHOULD_RECREATE_TABS = "should_recreate_tabs"
const val IS_QUEUE_HIDDEN_PEEK = "is_queue_hidden"
const val WIDGET_BACKGROUND = "widget_background"
const val PROGRESS_BAR_STYLE = "progress_bar_style"
const val INTRO_SHOWN = "intro_shown"
const val TOGGLE_MINI_SWIPE = "toggle_mini_swipe"
const val TOGGLE_AUTOPLAY = "toggle_autoplay"
const val AUTO_ROTATE = "auto_rotate"
const val WIDGET_PANEL = "widget_panel"
const val DEV_MODE = "dev_mode"
const val NOTIFICATION_ACTION_1 = "notification_action_1"
const val NOTIFICATION_ACTION_2 = "notification_action_2"
const val DISABLE_WIDGETS = "disable_widgets"
const val INTERNET_CONNECTED = "internet_connected"
const val WIDGET_BUTTON_COLOR = "widget_button_color"
const val QUEUE_STYLE = "queue_style"
const val QUEUE_STYLE_LAND = "queue_style_land"
const val PLAYER_BACKGROUND = "player_background"
const val BACKUP_PATH = "backup_path"
const val DISABLE_UPDATE = "disable_update"
const val SCROLLBAR_STYLE = "scrollbar_style"
const val COLOR_ANIMATE = "color_animate"
const val CAR_CONNECTED = "car_connected"
const val AUTO_ACTION_1 = "auto_action_1"
const val AUTO_ACTION_2 = "auto_action_2"
const val USE_NOTI_ACTIONS_AUTO = "use_noti_actions_auto"
const val INTRO_SLIDES_SHOWN = "intro_slides_shown"
const val SEARCH_ACTION = "search_action"
const val SEARCH_ICON_NAVIGATION = "search_icon_navigation"
const val SEARCH_FROM_NAVIGATION = "search_from_navigation"
const val VOLUME_CONTROLS = "volume_controls"
const val EQUALIZER_STOCK = "equalizer_stock"
const val FONT_SIZE = "font_size"
const val SHUFFLE_STATE = "shuffle_state"
const val REWIND_DURATION = "rewind_duration"
const val FAST_FORWARD_DURATION = "fast_forward_duration"
const val DURATION_SAME = "duration_same"
const val TRANSPARENT_MINI_PLAYER = "transparent_mini_player"
const val LYRICS_MODE = "lyrics_mode"
const val EMBED_LYRICS_ACTIVATED = "embed_lyrics_activated"
const val DISABLE_MESSAGE_LYRICS = "disable_message_lyrics"
const val SIMPLE_MODE = "simple_mode"