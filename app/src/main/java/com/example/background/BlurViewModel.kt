/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    private val workManager = WorkManager.getInstance()

    internal fun applyBlur(blurLevel: Int) {
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>().build()
        var continuation = workManager.beginWith(cleanupRequest)

        for(i in 0 until blurLevel) {
            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()

            // set input data only for the first operation. After the first blur operation the
            // input will be the output of previous blur operations.
            if(i == 0) {
                blurBuilder.setInputData(createInputDataForURI())
            }

            continuation = continuation.then(blurBuilder.build())
        }

        val saveRequest = OneTimeWorkRequestBuilder<SaveImageToFileWorker>().build()
        continuation.then(saveRequest).enqueue()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun createInputDataForURI(): Data {
        val dataBuilder = Data.Builder()
        imageUri?.let { dataBuilder.putString(KEY_IMAGE_URI, it.toString()) }
        return dataBuilder.build()
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}
