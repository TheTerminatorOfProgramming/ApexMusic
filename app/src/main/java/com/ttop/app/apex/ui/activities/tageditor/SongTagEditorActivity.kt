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
package com.ttop.app.apex.ui.activities.tageditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.shape.MaterialShapeDrawable
import com.ttop.app.apex.R
import com.ttop.app.apex.databinding.ActivitySongTagEditorBinding
import com.ttop.app.apex.extensions.*
import com.bumptech.glide.Glide
import com.ttop.app.apex.glide.ApexGlideExtension.asBitmapPalette
import com.ttop.app.apex.glide.palette.BitmapPaletteWrapper
import com.ttop.app.apex.model.ArtworkInfo
import com.ttop.app.apex.repository.SongRepository
import com.ttop.app.apex.ui.fragments.search.clearText
import com.ttop.app.apex.util.ApexColorUtil
import com.ttop.app.apex.util.ImageUtil
import com.ttop.app.apex.util.MusicUtil
import com.ttop.app.apex.util.logD
import com.ttop.app.appthemehelper.util.MaterialValueHelper
import com.xeinebiu.lyrics_finder.LyricsFinder
import kotlinx.coroutines.launch
import org.jaudiotagger.tag.FieldKey
import org.koin.android.ext.android.inject
import java.util.*


class SongTagEditorActivity : AbsTagEditorActivity<ActivitySongTagEditorBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySongTagEditorBinding =
        ActivitySongTagEditorBinding::inflate

    private val lyricsFinder = LyricsFinder()
    private val songRepository by inject<SongRepository>()

    private var albumArtBitmap: Bitmap? = null
    private var deleteAlbumArt: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViews()
        setSupportActionBar(binding.toolbar)
        binding.appBarLayout?.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_find_lyrics, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_find_lyrics -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.find_lyrics)
                builder.setMessage(R.string.lyrics_search_summary)

                builder.setPositiveButton(R.string.manually) { _, _ ->
                    showdialog()
                }

                builder.setNegativeButton(R.string.id3_tags) { _, _ ->
                    val query = binding.songText.text.toString() + " " + binding.artistText.text.toString()

                    lifecycleScope.launch {
                        binding.lyricsText.clearText()
                        val lyrics = lyricsFinder.find(query)
                        var modifiedLyrics = lyrics.toString()
                        modifiedLyrics = modifiedLyrics.replace("\\[.+\\]\\s?".toRegex(), "\n").trim()
                        modifiedLyrics = modifiedLyrics.replace("\n\n\n", "\n\n").trim()
                        modifiedLyrics = modifiedLyrics.replace("\n\n\n", "\n").trim()
                        binding.lyricsText.setText(modifiedLyrics)
                    }
                }
                builder.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showdialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.title)

// Set up the input
        val input = EditText(this)
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.hint = getString(R.string.enter_text)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

// Set up the buttons
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            // Here you get get input text from the Edittext
            val m_Text = input.text.toString()

            lifecycleScope.launch {
                binding.lyricsText.clearText()
                val lyrics = lyricsFinder.find(m_Text)
                var modifiedLyrics = lyrics.toString()
                modifiedLyrics = modifiedLyrics.replace("\\[.+\\]\\s?".toRegex(), "\n").trim()
                modifiedLyrics = modifiedLyrics.replace("\n\n\n", "\n\n").trim()
                modifiedLyrics = modifiedLyrics.replace("\n\n\n", "\n").trim()
                binding.lyricsText.setText(modifiedLyrics)
            }
        })
        builder.setNegativeButton(R.string.action_cancel, DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpViews() {
        fillViewsWithFileTags()
        binding.songTextContainer.setTint(false)
        binding.composerContainer.setTint(false)
        binding.albumTextContainer.setTint(false)
        binding.artistContainer.setTint(false)
        binding.albumArtistContainer.setTint(false)
        binding.yearContainer.setTint(false)
        binding.genreContainer.setTint(false)
        binding.trackNumberContainer.setTint(false)
        binding.discNumberContainer.setTint(false)
        binding.lyricsContainer.setTint(false)

        binding.songText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.albumText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.albumArtistText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.artistText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.genreText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.yearText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.trackNumberText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.discNumberText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.lyricsText.appHandleColor().doAfterTextChanged { dataChanged() }
        binding.songComposerText.appHandleColor().doAfterTextChanged { dataChanged() }
    }

    private fun fillViewsWithFileTags() {
        binding.songText.setText(songTitle)
        binding.albumArtistText.setText(albumArtist)
        binding.albumText.setText(albumTitle)
        binding.artistText.setText(artistName)
        binding.genreText.setText(genreName)
        binding.yearText.setText(songYear)
        binding.trackNumberText.setText(trackNumber)
        binding.discNumberText.setText(discNumber)
        binding.lyricsText.setText(lyrics)
        binding.songComposerText.setText(composer)
        logD(songTitle + songYear)
    }

    override fun loadCurrentImage() {
        val bitmap = albumArt
        setImageBitmap(
            bitmap,
            ApexColorUtil.getColor(
                ApexColorUtil.generatePalette(bitmap),
                defaultFooterColor()
            )
        )
        deleteAlbumArt = false
    }

    override fun searchImageOnWeb() {
        searchWebFor(binding.songText.text.toString(), binding.artistText.text.toString())
    }

    override fun deleteImage() {
        setImageBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.default_audio_art),
            defaultFooterColor()
        )
        deleteAlbumArt = true
        dataChanged()
    }

    override fun setColors(color: Int) {
        super.setColors(color)
        saveFab.backgroundTintList = ColorStateList.valueOf(color)
        ColorStateList.valueOf(
            MaterialValueHelper.getPrimaryTextColor(
                this,
                color.isColorLight
            )
        ).also {
            saveFab.iconTint = it
            saveFab.setTextColor(it)
        }
    }

    override fun save() {
        val fieldKeyValueMap = EnumMap<FieldKey, String>(FieldKey::class.java)
        fieldKeyValueMap[FieldKey.TITLE] = binding.songText.text.toString()
        fieldKeyValueMap[FieldKey.ALBUM] = binding.albumText.text.toString()
        fieldKeyValueMap[FieldKey.ARTIST] = binding.artistText.text.toString()
        fieldKeyValueMap[FieldKey.GENRE] = binding.genreText.text.toString()
        fieldKeyValueMap[FieldKey.YEAR] = binding.yearText.text.toString()
        fieldKeyValueMap[FieldKey.TRACK] = binding.trackNumberText.text.toString()
        fieldKeyValueMap[FieldKey.DISC_NO] = binding.discNumberText.text.toString()
        fieldKeyValueMap[FieldKey.LYRICS] = binding.lyricsText.text.toString()
        fieldKeyValueMap[FieldKey.ALBUM_ARTIST] = binding.albumArtistText.text.toString()
        fieldKeyValueMap[FieldKey.COMPOSER] = binding.songComposerText.text.toString()
        writeValuesToFiles(
            fieldKeyValueMap, when {
                deleteAlbumArt -> ArtworkInfo(id, null)
                albumArtBitmap == null -> null
                else -> ArtworkInfo(id, albumArtBitmap!!)
            }
        )
    }

    override fun getSongPaths(): List<String> = listOf(songRepository.song(id).data)

    override fun getSongUris(): List<Uri> = listOf(MusicUtil.getSongFileUri(id))

    override fun loadImageFromFile(selectedFile: Uri?) {
        Glide.with(this@SongTagEditorActivity).asBitmapPalette().load(selectedFile)
            .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
            .into(object : ImageViewTarget<BitmapPaletteWrapper>(binding.editorImage) {
                override fun onResourceReady(
                    resource: BitmapPaletteWrapper,
                    transition: Transition<in BitmapPaletteWrapper>?
                ) {
                    ApexColorUtil.getColor(resource.palette, Color.TRANSPARENT)
                    albumArtBitmap = resource.bitmap?.let { ImageUtil.resizeBitmap(it, 2048) }
                    setImageBitmap(
                        albumArtBitmap,
                        ApexColorUtil.getColor(
                            resource.palette,
                            defaultFooterColor()
                        )
                    )
                    deleteAlbumArt = false
                    dataChanged()
                    setResult(Activity.RESULT_OK)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    showToast(R.string.error_load_failed, Toast.LENGTH_LONG)
                }

                override fun setResource(resource: BitmapPaletteWrapper?) {}
            })
    }

    companion object {
        val TAG: String = SongTagEditorActivity::class.java.simpleName
    }

    override val editorImage: ImageView
        get() = binding.editorImage
}
