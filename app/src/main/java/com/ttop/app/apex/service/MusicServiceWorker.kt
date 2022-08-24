package com.ttop.app.apex.service

import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ttop.app.apex.util.ApexUtil

class MusicServiceWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        setForegroundAsync(createForegroundInfo())

        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1,
            ApexUtil.createForegroundInfo(1, "foreground_notification")
        )
    }
}