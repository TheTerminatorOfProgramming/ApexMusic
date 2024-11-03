/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.ttop.app.apex.libraries.fastscroller;

import androidx.annotation.NonNull;

public interface ViewHelperProvider {

    @NonNull
    FastScroller.ViewHelper getViewHelper();
}
