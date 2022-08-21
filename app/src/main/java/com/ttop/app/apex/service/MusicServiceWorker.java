package com.ttop.app.apex.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ttop.app.apex.util.ApexUtil;

public class MusicServiceWorker extends Worker {

    private static final String TAG = "MusicServiceWorker";
    String NOTIFICATION_CHANNEL_ID = "foreground_notification";
    int NOTIFICATION_ID = 1;
    public MusicServiceWorker (@NonNull Context context, @NonNull WorkerParameters workerParams ) {
        super ( context, workerParams );
    }

    @NonNull
    @Override
    public Result doWork () {
        setForegroundAsync(createForegroundInfo());
        return Result.success ();
    }

    public ForegroundInfo createForegroundInfo() {
        return new ForegroundInfo(NOTIFICATION_ID, ApexUtil.INSTANCE.createNotification(NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID));
    }
}