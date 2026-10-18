package ru.tulsu.mobileprogramming.imagecounter

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var counterText: TextView
    private lateinit var hintText: TextView
    private lateinit var clickImage: ImageView
    private var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterText = findViewById(R.id.counterText)
        hintText = findViewById(R.id.hintText)
        clickImage = findViewById(R.id.clickImage)

        clickCount = savedInstanceState?.getInt(KEY_CLICK_COUNT, 0) ?: 0
        showCount()

        clickImage.setOnClickListener {
            clickCount++
            showCount()
            animateClick(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_CLICK_COUNT, clickCount)
        super.onSaveInstanceState(outState)
    }

    private fun showCount() {
        counterText.text = resources.getQuantityString(R.plurals.click_count, clickCount, clickCount)
        hintText.text = getString(
            if (clickCount == 0) R.string.hint_initial else R.string.hint_after_click
        )
    }

    private fun animateClick(view: View) {
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(90)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(140)
                    .start()
            }
            .start()
    }

    private companion object {
        const val KEY_CLICK_COUNT = "click_count"
    }
}
