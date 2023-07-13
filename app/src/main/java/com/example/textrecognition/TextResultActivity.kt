package com.example.textrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.example.textrecognition.databinding.ActivityTextRecognitionBinding
import com.example.textrecognition.databinding.ActivityTextResultBinding
import java.util.Locale

class TextResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextResultBinding
    private var textResult: String? = null
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                // Set the language for text-to-speech
                textToSpeech.language = Locale.US
            }
        }

        textResult = intent.getStringExtra("result")

        if (textResult != null)
            binding.textResult.text = textResult

        binding.buttonSpeaker.setOnClickListener {
            if (textResult != null) {
                textToSpeech.speak(textResult, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shutdown the Text-to-Speech engine
        textToSpeech.shutdown()
    }
}