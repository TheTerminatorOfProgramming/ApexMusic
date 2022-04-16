package code.name.monkey.retro.interfaces

import android.view.View
import code.name.monkey.retro.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}