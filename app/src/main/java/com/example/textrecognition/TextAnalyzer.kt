package com.example.textrecognition

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.textrecognition.databinding.ActivityTextRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextAnalyzer : ImageAnalysis.Analyzer {
    var context: Context? = null
    var binding: ActivityTextRecognitionBinding? = null


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = textRecognizer.process(image)
                .addOnSuccessListener {
                    val listTextBlock = it.textBlocks
                    binding?.textBoxOverlay?.setTextBlocks(
                        listTextBlock,
                        imageProxy.image!!.width.toFloat(),
                        imageProxy.image!!.height.toFloat(),
                        TextRecognitionActivity.isBackCam)
                }
                .addOnFailureListener {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}