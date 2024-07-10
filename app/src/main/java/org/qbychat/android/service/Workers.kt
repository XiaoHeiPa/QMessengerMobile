package org.qbychat.android.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AccountWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        return Result.success()
    }
}