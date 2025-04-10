package com.quanhspdz.textrecognition

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.quanhspdz.textrecognition.databinding.ActivityTextRecognitionBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TextRecognitionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextRecognitionBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraFacing = CameraSelector.DEFAULT_BACK_CAMERA

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val resultUri = result.uriContent
            scanTextFromImage(resultUri)
        } else {
            Toast.makeText(this, "Error cropping image", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

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
                Toast.makeText(
                    this,
                    "App does not have permission to access your phone storage!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonSwitchVisible.setOnClickListener {
            isVisible = !isVisible
            binding.buttonSwitchVisible.setImageResource(
                if (isVisible) R.drawable.ic_invisible else R.drawable.ic_visible
            )
            binding.camPreview.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun requestAllPermissions() {
        if (checkPermission()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Constants.REQUIRED_PERMISSION)
        }
    }

    private fun checkPermission(): Boolean {
        return Constants.REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCamera() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "This device has no camera", Toast.LENGTH_LONG).show()
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.camPreview.surfaceProvider)
                    }

                val textAnalyzer = TextAnalyzer().apply {
                    context = this@TextRecognitionActivity
                    binding = this@TextRecognitionActivity.binding
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, textAnalyzer)
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraFacing,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "startCamera() failed: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun scanTextFromImage(resultUri: Uri?) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
            val image = InputImage.fromBitmap(bitmap, 0)

            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            textRecognizer.process(image)
                .addOnSuccessListener {
                    val listTextBlock = it.textBlocks
                    val text = TextAnalyzer.getText(listTextBlock)
                    val intent = Intent(this, TextResultActivity::class.java)
                    intent.putExtra("result", text)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, Constants.IMAGE_PICKER_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                cropImageLauncher.launch(
                    CropImageContractOptions(uri, CropImageOptions())
                )
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
}
