package com.ttop.app.apex.ui.activities.tageditor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.util.Log
import com.ttop.app.apex.R
import com.ttop.app.apex.extensions.showToast
import com.ttop.app.apex.misc.UpdateToastMediaScannerCompletionListener
import com.ttop.app.apex.model.AudioTagInfo
import com.ttop.app.apex.util.MusicUtil.createAlbumArtFile
import com.ttop.app.apex.util.MusicUtil.deleteAlbumArt
import com.ttop.app.apex.util.MusicUtil.insertAlbumArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.CannotWriteException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.FieldDataInvalidException
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.images.AndroidArtwork
import org.jaudiotagger.tag.images.Artwork
import java.io.File
import java.io.IOException

class TagWriter {

    companion object {

        suspend fun scan(context: Context, toBeScanned: List<String?>?) {
            if (toBeScanned.isNullOrEmpty()) {
                Log.i("scan", "scan: Empty")
                context.showToast(R.string.scan_from_folder)
                return
            }
            MediaScannerConnection.scanFile(
                context,
                toBeScanned.toTypedArray(),
                null,
                withContext(Dispatchers.Main) {
                    if (context is Activity) UpdateToastMediaScannerCompletionListener(
                        context, toBeScanned
                    ) else null
                }
            )
        }

        suspend fun writeTagsToFiles(context: Context, info: AudioTagInfo) {
            withContext(Dispatchers.IO) {
                var artwork: Artwork? = null
                var albumArtFile: File? = null
                if (info.artworkInfo?.artwork != null) {
                    try {
                        albumArtFile = createAlbumArtFile(context).canonicalFile
                        info.artworkInfo.artwork.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            albumArtFile.outputStream()
                        )
                        artwork = AndroidArtwork.createArtworkFromFile(albumArtFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                var wroteArtwork = false
                var deletedArtwork = false
                for (filePath in info.filePaths!!) {
                    try {
                        val audioFile = AudioFileIO.read(File(filePath))
                        val tag = audioFile.tagOrCreateAndSetDefault
                        if (info.fieldKeyValueMap != null) {
                            for ((key, newValue) in info.fieldKeyValueMap) {
                                try {
                                    val currentValue = tag.getFirst(key)
                                    if (currentValue != newValue) {
                                        if (newValue.isEmpty()) {
                                            tag.deleteField(key)
                                        } else {
                                            tag.setField(key, newValue)
                                        }
                                    }
                                } catch (e: FieldDataInvalidException) {
                                    withContext(Dispatchers.Main) {
                                        context.showToast(R.string.could_not_write_tags_to_file)
                                    }
                                    return@withContext listOf<File>()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField()
                                deletedArtwork = true
                            } else if (artwork != null) {
                                tag.deleteArtworkField()
                                tag.setField(artwork)
                                wroteArtwork = true
                            }
                        }
                        audioFile.commit()
                    } catch (e: CannotReadException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: CannotWriteException) {
                        e.printStackTrace()
                    } catch (e: TagException) {
                        e.printStackTrace()
                    } catch (e: ReadOnlyFileException) {
                        e.printStackTrace()
                    } catch (e: InvalidAudioFrameException) {
                        e.printStackTrace()
                    }
                }
                if (wroteArtwork) {
                    insertAlbumArt(context, info.artworkInfo!!.albumId, albumArtFile!!.path)
                } else if (deletedArtwork) {
                    deleteAlbumArt(context, info.artworkInfo!!.albumId)
                }
                scan(context, info.filePaths)
            }
        }

        suspend fun writeTagsToFilesR(context: Context, info: AudioTagInfo): List<File> =
            withContext(Dispatchers.IO) {
                val cacheFiles = mutableListOf<File>()
                var artwork: Artwork? = null
                var albumArtFile: File? = null
                if (info.artworkInfo?.artwork != null) {
                    try {
                        albumArtFile = createAlbumArtFile(context).canonicalFile
                        info.artworkInfo.artwork.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            albumArtFile.outputStream()
                        )
                        artwork = AndroidArtwork.createArtworkFromFile(albumArtFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                var wroteArtwork = false
                var deletedArtwork = false
                for (filePath in info.filePaths!!) {
                    try {
                        val originFile = File(filePath)
                        val cacheFile = File(context.cacheDir, originFile.name)
                        cacheFiles.add(cacheFile)
                        originFile.inputStream().use { input ->
                            cacheFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        val audioFile = AudioFileIO.read(cacheFile)
                        val tag = audioFile.tagOrCreateAndSetDefault
                        if (info.fieldKeyValueMap != null) {
                            for ((key, newValue) in info.fieldKeyValueMap) {
                                try {
                                    val currentValue = tag.getFirst(key)
                                    if (currentValue != newValue) {
                                        if (newValue.isEmpty()) {
                                            tag.deleteField(key)
                                        } else {
                                            tag.setField(key, newValue)
                                        }
                                    }
                                } catch (e: FieldDataInvalidException) {
                                    withContext(Dispatchers.Main) {
                                        context.showToast(R.string.could_not_write_tags_to_file)
                                    }
                                    return@withContext listOf<File>()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField()
                                deletedArtwork = true
                            } else if (artwork != null) {
                                tag.deleteArtworkField()
                                tag.setField(artwork)
                                wroteArtwork = true
                            }
                        }
                        audioFile.commit()
                    } catch (e: CannotReadException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: CannotWriteException) {
                        e.printStackTrace()
                    } catch (e: TagException) {
                        e.printStackTrace()
                    } catch (e: ReadOnlyFileException) {
                        e.printStackTrace()
                    } catch (e: InvalidAudioFrameException) {
                        e.printStackTrace()
                    }
                }
                if (wroteArtwork) {
                    insertAlbumArt(context, info.artworkInfo!!.albumId, albumArtFile!!.path)
                } else if (deletedArtwork) {
                    deleteAlbumArt(context, info.artworkInfo!!.albumId)
                }
                cacheFiles
            }
    }
}