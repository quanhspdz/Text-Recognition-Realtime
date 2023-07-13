package com.example.textrecognition

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.textrecognition.Constants.IMAGE_PICKER_REQUEST_CODE
import com.example.textrecognition.databinding.ActivityTextRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.theartofdev.edmodo.cropper.CropImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class TextRecognitionActivity : AppCompatActivity() {

    lateinit var binding: ActivityTextRecognitionBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraFacing = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextRecognitionBinding.inflate(layoutInflater)
        makeFullScreen()
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        requestAllPermissions()

        binding.buttonPickImage.setOnClickListener {
            if (checkPermission()) {
                openImagePicker()
            } else {
                Toast.makeText(this, "App does not have permission to access your phone storage!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonSwitchVisible.setOnClickListener {
            if (isVisible) {
                isVisible = false
                binding.buttonSwitchVisible.setImageResource(R.drawable.ic_visible)
                binding.camPreview.visibility = View.INVISIBLE
            } else {
                isVisible = true
                binding.buttonSwitchVisible.setImageResource(R.drawable.ic_invisible)
                binding.camPreview.visibility = View.VISIBLE
            }
        }
    }

    private fun requestAllPermissions() {
        if (checkPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                Constants.REQUIRED_PERMISSION,
                Constants.CAMERA_REQUEST_CODE_PERMISSION)
        }
    }

    private fun checkPermission() =
        Constants.REQUIRED_PERMISSION.all{
            ContextCompat.checkSelfPermission(
                baseContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.camPreview.surfaceProvider)
                }

            val textAnalyzer = TextAnalyzer()
            textAnalyzer.context = this@TextRecognitionActivity
            textAnalyzer.binding = binding

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, textAnalyzer)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraFacing,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CAMERA_REQUEST_CODE_PERMISSION) {
            if (checkPermission()) {
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show()
                startCamera()
            } else {
                Toast.makeText(this, "App does not have permission to access camera!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun makeFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()
    }

    companion object {
        var isBackCam = true
        var isVisible = true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                try {
                    // start cropping activity for pre-acquired image saved on the device
                    CropImage.activity(selectedImageUri)
                        .start(this);
                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                val resultUri = result.uri
                scanTextFromImage(resultUri)
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun scanTextFromImage(resultUri: Uri?) {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
        val image = InputImage.fromBitmap(bitmap, 0)

        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = textRecognizer.process(image)
            .addOnSuccessListener {
                val listTextBlock = it.textBlocks
                val text = TextAnalyzer.getText(listTextBlock)
                intent = Intent(this, TextResultActivity::class.java)
                intent.putExtra("result", text)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }
}