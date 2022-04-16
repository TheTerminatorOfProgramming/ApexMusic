package com.ttop.app.appthemehelper.common

import androidx.appcompat.widget.Toolbar

import com.ttop.app.appthemehelper.util.ToolbarContentTintHelper

class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
