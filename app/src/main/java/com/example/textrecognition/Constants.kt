package com.example.textrecognition

import android.Manifest

object Constants {
    const val CAMERA_REQUEST_CODE_PERMISSION = 123
    const val IMAGE_PICKER_REQUEST_CODE = 3453
    val REQUIRED_PERMISSION = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE)
}