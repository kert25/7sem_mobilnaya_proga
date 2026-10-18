package ru.tulsu.mobileprogramming.mooddialog

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var nameInput: EditText
    private lateinit var goodMoodButton: RadioButton
    private lateinit var resultText: TextView
    private lateinit var moodImage: ImageView
    private var greetingShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.nameInput)
        goodMoodButton = findViewById(R.id.goodMoodButton)
        resultText = findViewById(R.id.resultText)
        moodImage = findViewById(R.id.moodImage)

        savedInstanceState?.let {
            nameInput.setText(it.getString(KEY_NAME, ""))
            goodMoodButton.isChecked = it.getBoolean(KEY_GOOD_MOOD, true)
            greetingShown = it.getBoolean(KEY_GREETING_SHOWN, false)
            if (greetingShown) {
                showGreeting()
            }
        }

        findViewById<Button>(R.id.greetButton).setOnClickListener {
            greetingShown = true
            showGreeting()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_NAME, nameInput.text.toString())
        outState.putBoolean(KEY_GOOD_MOOD, goodMoodButton.isChecked)
        outState.putBoolean(KEY_GREETING_SHOWN, greetingShown)
        super.onSaveInstanceState(outState)
    }

    private fun showGreeting() {
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            greetingShown = false
            nameInput.error = getString(R.string.name_required)
            resultText.text = getString(R.string.enter_name_prompt)
            moodImage.setImageResource(R.drawable.mood_neutral)
            return
        }

        val isGoodMood = goodMoodButton.isChecked
        val moodText = getString(if (isGoodMood) R.string.mood_good else R.string.mood_bad)
        resultText.text = getString(R.string.greeting_template, name, moodText)
        moodImage.setImageResource(if (isGoodMood) R.drawable.mood_happy else R.drawable.mood_sad)
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_GOOD_MOOD = "good_mood"
        const val KEY_GREETING_SHOWN = "greeting_shown"
    }
}
