package com.quanhspdz.textrecognition

import android.Manifest
import android.os.Build

object Constants {
    const val CAMERA_REQUEST_CODE_PERMISSION = 123
    const val IMAGE_PICKER_REQUEST_CODE = 3453

    val REQUIRED_PERMISSION: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}
