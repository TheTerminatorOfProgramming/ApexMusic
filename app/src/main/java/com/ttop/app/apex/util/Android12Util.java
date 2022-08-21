package com.ttop.app.apex.util;

import android.content.Context;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.ttop.app.apex.service.MusicServiceWorker;
import org.jetbrains.annotations.NotNull;

public class Android12Util {

    public static void StartForegroundService(@NotNull Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder ( MusicServiceWorker.class ).addTag ( "MUSIC_SERVICE_WORKER" ).build ();
        WorkManager.getInstance (context).enqueue ( request );
    }
}
