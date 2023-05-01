package com.ttop.app.apex

import androidx.room.Room
import com.ttop.app.apex.auto.AutoMusicProvider
import com.ttop.app.apex.cast.ApexWebServer
import com.ttop.app.apex.db.ApexDatabase
import com.ttop.app.apex.db.MIGRATION_23_24
import com.ttop.app.apex.model.Genre
import com.ttop.app.apex.network.provideDefaultCache
import com.ttop.app.apex.network.provideLastFmRest
import com.ttop.app.apex.network.provideLastFmRetrofit
import com.ttop.app.apex.network.provideOkHttp
import com.ttop.app.apex.repository.*
import com.ttop.app.apex.ui.fragments.LibraryViewModel
import com.ttop.app.apex.ui.fragments.albums.AlbumDetailsViewModel
import com.ttop.app.apex.ui.fragments.artists.ArtistDetailsViewModel
import com.ttop.app.apex.ui.fragments.genres.GenreDetailsViewModel
import com.ttop.app.apex.ui.fragments.playlists.PlaylistDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {

    factory {
        provideDefaultCache()
    }
    factory {
        provideOkHttp(get(), get())
    }
    single {
        provideLastFmRetrofit(get())
    }
    single {
        provideLastFmRest(get())
    }
}

private val roomModule = module {

    single {
        Room.databaseBuilder(androidContext(), ApexDatabase::class.java, "playlist.db")
            .addMigrations(MIGRATION_23_24)
            .build()
    }

    factory {
        get<ApexDatabase>().playlistDao()
    }

    factory {
        get<ApexDatabase>().playCountDao()
    }

    factory {
        get<ApexDatabase>().historyDao()
    }

    single {
        RealRoomRepository(get(), get(), get())
    } bind RoomRepository::class
}
private val autoModule = module {
    single {
        AutoMusicProvider(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
private val mainModule = module {
    single {
        androidContext().contentResolver
    }
    single {
        ApexWebServer(get())
    }
}
private val dataModule = module {
    single {
        RealRepository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    } bind Repository::class

    single {
        RealSongRepository(get())
    } bind SongRepository::class

    single {
        RealGenreRepository(get(), get())
    } bind GenreRepository::class

    single {
        RealAlbumRepository(get())
    } bind AlbumRepository::class

    single {
        RealArtistRepository(get(), get())
    } bind ArtistRepository::class

    single {
        RealPlaylistRepository(get())
    } bind PlaylistRepository::class

    single {
        RealTopPlayedRepository(get(), get(), get(), get())
    } bind TopPlayedRepository::class

    single {
        RealLastAddedRepository(
            get(),
            get(),
            get()
        )
    } bind LastAddedRepository::class

    single {
        RealSearchRepository(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single {
        RealLocalDataRepository(get())
    } bind LocalDataRepository::class
}

private val viewModules = module {

    viewModel {
        LibraryViewModel(get())
    }

    viewModel { (albumId: Long) ->
        AlbumDetailsViewModel(
            get(),
            albumId
        )
    }

    viewModel { (artistId: Long?, artistName: String?) ->
        ArtistDetailsViewModel(
            get(),
            artistId,
            artistName
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailsViewModel(
            get(),
            playlistId
        )
    }

    viewModel { (genre: Genre) ->
        GenreDetailsViewModel(
            get(),
            genre
        )
    }
}

val appModules = listOf(mainModule, dataModule, autoModule, viewModules, networkModule, roomModule)