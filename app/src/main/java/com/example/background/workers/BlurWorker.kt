package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.TAG


class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val appContext = applicationContext
        // Get data that was passed from WorkRequest
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        return try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input URI")
                throw IllegalArgumentException("Invalid Input URI")
            }

            val resolver = appContext.contentResolver
            val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))
            val blurredPicture = blurBitmap(picture, appContext)
            val outputUri = writeBitmapToFile(appContext, blurredPicture)
            makeStatusNotification("Output: $outputUri", appContext)
            // Create output data
            val outPutData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outPutData)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur", throwable)
            Result.failure()
        }
    }
}
