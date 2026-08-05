package com.example.vigil.detection

import android.app.job.JobParameters
import android.app.job.JobService
import kotlin.concurrent.thread

class PendingDetectionJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        thread {
            PendingDetections.checkPending(applicationContext) {
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean = true
}
