package com.ttop.app.apex.libraries.appthemehelper.common

import androidx.appcompat.widget.Toolbar
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper


class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
