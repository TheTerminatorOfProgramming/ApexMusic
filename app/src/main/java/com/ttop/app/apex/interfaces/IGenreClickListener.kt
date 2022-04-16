package com.ttop.app.apex.interfaces

import android.view.View
import com.ttop.app.apex.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}