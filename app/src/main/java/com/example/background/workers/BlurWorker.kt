package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.R
import com.example.background.TAG

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val appContext = applicationContext
        return try {
            val picture = BitmapFactory.decodeResource(appContext.resources, R.drawable.test)
            val blurredPicture = blurBitmap(picture, appContext)
            val blurredPictureUri = writeBitmapToFile(appContext, blurredPicture)
            makeStatusNotification("Output: $blurredPictureUri", appContext)
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur", throwable)
            Result.failure()
        }
    }
}
