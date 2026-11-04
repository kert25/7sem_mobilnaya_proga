package ru.tulsu.mobileprogramming.businesscard

import android.app.Activity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val avatar = findViewById<ImageView>(R.id.avatarImage)
        avatar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.avatar_entrance))

        val timeTextView = findViewById<TextView>(R.id.timeTextView)
        val showTimeButton = findViewById<Button>(R.id.showTimeButton)

        showTimeButton.setOnClickListener {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            timeTextView.text = currentTime
        }
    }
}
