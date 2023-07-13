package com.example.textrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.textrecognition.databinding.ActivityTextRecognitionBinding
import com.example.textrecognition.databinding.ActivityTextResultBinding

class TextResultActivity : AppCompatActivity() {
    lateinit var binding: ActivityTextResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textResult = intent.getStringExtra("result")

        if (textResult != null)
            binding.textResult.text = textResult
    }
}