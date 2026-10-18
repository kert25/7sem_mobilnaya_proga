package ru.tulsu.mobileprogramming.businesscard

import android.app.Activity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val avatar = findViewById<ImageView>(R.id.avatarImage)
        avatar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.avatar_entrance))
    }
}
