package com.ttop.app.apex.extensions

import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.ttop.app.apex.model.Song
import com.ttop.app.apex.util.ApexUtil
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.net.URLEncoder

fun getSongInfo(song: Song): String {
    val file = File(song.data)
    if (file.exists()) {
        return try {
            val audioHeader = AudioFileIO.read(File(song.data)).audioHeader
            val string: StringBuilder = StringBuilder()
            val uriFile = file.toUri()
            if (getMimeType(uriFile.toString()) == "FLAC"){
                string.append(audioHeader.format).append(" • ")
            }else{
                string.append(getMimeType(uriFile.toString())).append(" • ")
            }
            string.append(audioHeader.bitRate).append(" kb/s").append(" • ")
            string.append(ApexUtil.frequencyCount(audioHeader.sampleRate.toInt()))
                .append(" kHz")
            string.toString()
        } catch (er: Exception) {
            "Error: $er"
        }
    }
    return "-"
}

private fun getMimeType(url: String): String {
    var type: String? = MimeTypeMap.getFileExtensionFromUrl(
        URLEncoder.encode(url, "utf-8")
    ).uppercase()
    if (type == null) {
        type = url.substring(url.lastIndexOf(".") + 1)
    }
    return type
}