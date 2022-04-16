package code.name.monkey.retro.interfaces

import code.name.monkey.retro.model.Album
import code.name.monkey.retro.model.Artist
import code.name.monkey.retro.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}