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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import com.bumptech.glide.Glide


class BlurActivity : AppCompatActivity() {

    private lateinit var viewModel: BlurViewModel
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var goButton: Button
    private lateinit var seeFileButton: Button
    private lateinit var cancelButton: Button
    private lateinit var radioGroup: RadioGroup

    private val blurLevel: Int
        get() =
            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blur)
        bindResources()

        // Get the ViewModel
        viewModel = ViewModelProviders.of(this).get(BlurViewModel::class.java)

        // Image uri should be stored in the ViewModel, because when the activity is destroyed,
        // the uri will still be there in ViewModel. Put it there then display the image.
        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(imageView)
        }

        setOnClickListeners()

        viewModel.outputWorkInfos.observe(this, workInfosObserver())
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { workInfoList ->
            if(workInfoList.isNullOrEmpty()) {
                return@Observer
            }
            val workInfo:WorkInfo = workInfoList[0]
            if(workInfo.state.isFinished) {
                showWorkFinished()
                // Normally this processing, which is not directly related to drawing views on
                // screen would be in the ViewModel. For simplicity we are keeping it here.
                processOutputDataAndEnableSeeFileButton(workInfo)
            } else {
                showWorkInProgress()
            }
        }
    }

    private fun processOutputDataAndEnableSeeFileButton(workInfo: WorkInfo) {
        val outputData = workInfo.outputData
        val outputUri = outputData.getString(KEY_IMAGE_URI)
        if (!outputUri.isNullOrEmpty()) {
            viewModel.setOutputUri(outputUri)
            seeFileButton.visibility = View.VISIBLE
        }
    }

    private fun setOnClickListeners() {
        goButton.setOnClickListener {
            viewModel.applyBlur(blurLevel)
        }

        seeFileButton.setOnClickListener {
            viewModel.outputUri?.let { currentUri ->
                // This is how you view image on the Activity of some other app
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        /**
         * This looks like a better code than the code above. Commented it for future reference.
         */
//        seeFileButton.setOnClickListener {
//            viewModel.outputUri?.let { currentUri ->
//                val intent = Intent(Intent.ACTION_VIEW, currentUri)
//                // Name of the component implementing an activity that can display this intent.
//                val componentName = intent.resolveActivity(packageManager)
//                // Check whether the component that can display this intent exists (not null).
//                if(componentName != null) startActivity(intent)
//            }
//        }

        cancelButton.setOnClickListener { viewModel.cancelWork() }
    }

    private fun bindResources() {
        imageView = findViewById(R.id.image_view)
        progressBar = findViewById(R.id.progress_bar)
        goButton = findViewById(R.id.go_button)
        seeFileButton = findViewById(R.id.see_file_button)
        cancelButton = findViewById(R.id.cancel_button)
        radioGroup = findViewById(R.id.radio_blur_group)
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        progressBar.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE
        goButton.visibility = View.GONE
        seeFileButton.visibility = View.GONE
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        progressBar.visibility = View.GONE
        cancelButton.visibility = View.GONE
        goButton.visibility = View.VISIBLE
    }
}
