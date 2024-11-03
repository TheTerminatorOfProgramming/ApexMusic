package com.ttop.app.apex.libraries.appthemehelper.common;

import android.graphics.Color;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ttop.app.apex.R;
import com.ttop.app.apex.libraries.appthemehelper.util.ATHUtil;
import com.ttop.app.apex.libraries.appthemehelper.util.ToolbarContentTintHelper;


public class ATHToolbarActivity extends com.ttop.app.apex.libraries.appthemehelper.ATHActivity {

    private Toolbar toolbar;

    public static int getToolbarBackgroundColor(@Nullable Toolbar toolbar) {
        if (toolbar != null) {
            return ATHUtil.INSTANCE.resolveColor(toolbar.getContext(), R.attr.colorSurface);
        }
        return Color.BLACK;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = getATHToolbar();
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(this, toolbar, menu, getToolbarBackgroundColor(toolbar));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(this, getATHToolbar(), com.ttop.app.apex.libraries.appthemehelper.ThemeStore.Companion.accentColor(getApplicationContext()));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        this.toolbar = toolbar;
        super.setSupportActionBar(toolbar);
    }

    protected Toolbar getATHToolbar() {
        return toolbar;
    }
}