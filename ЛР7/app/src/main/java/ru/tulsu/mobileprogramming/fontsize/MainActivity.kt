package ru.tulsu.mobileprogramming.fontsize

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Экран изменения размера шрифта надписи и цвета фона.
 *
 * Размер надписи меняется кнопками «Мельче» и «Крупнее» в диапазоне
 * MIN_TEXT_SIZE_SP..MAX_TEXT_SIZE_SP, кнопка «Сброс» возвращает исходное
 * значение. Три кнопки палитры перекрашивают фон в цвета из colors.xml.
 * Текущий размер и выбранный цвет сохраняются при пересоздании Activity.
 */
class MainActivity : Activity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var sampleText: TextView
    private lateinit var sizeValue: TextView
    private lateinit var smallerButton: Button
    private lateinit var largerButton: Button

    private var textSizeSp = DEFAULT_TEXT_SIZE_SP
    private var backgroundColorRes = R.color.redColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.root_layout)
        sampleText = findViewById(R.id.sampleText)
        sizeValue = findViewById(R.id.sizeValue)
        smallerButton = findViewById(R.id.smallerButton)
        largerButton = findViewById(R.id.largerButton)

        if (savedInstanceState != null) {
            textSizeSp = savedInstanceState.getInt(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE_SP)
            backgroundColorRes = savedInstanceState.getInt(KEY_BACKGROUND, R.color.redColor)
        }

        applyTextSize()
        applyBackground()

        smallerButton.setOnClickListener { changeTextSize(-STEP_SP) }
        largerButton.setOnClickListener { changeTextSize(STEP_SP) }
        findViewById<Button>(R.id.resetButton).setOnClickListener {
            textSizeSp = DEFAULT_TEXT_SIZE_SP
            applyTextSize()
        }

        findViewById<Button>(R.id.redButton).setOnClickListener { pickBackground(R.color.redColor) }
        findViewById<Button>(R.id.yellowButton).setOnClickListener { pickBackground(R.color.yellowColor) }
        findViewById<Button>(R.id.greenButton).setOnClickListener { pickBackground(R.color.greenColor) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_TEXT_SIZE, textSizeSp)
        outState.putInt(KEY_BACKGROUND, backgroundColorRes)
        super.onSaveInstanceState(outState)
    }

    /** Применяет текущий размер к надписи, счётчику и доступности кнопок. */
    private fun applyTextSize() {
        sampleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat())
        sizeValue.text = getString(R.string.size_value, textSizeSp)

        smallerButton.isEnabled = textSizeSp > MIN_TEXT_SIZE_SP
        largerButton.isEnabled = textSizeSp < MAX_TEXT_SIZE_SP
        smallerButton.alpha = if (smallerButton.isEnabled) 1f else DISABLED_ALPHA
        largerButton.alpha = if (largerButton.isEnabled) 1f else DISABLED_ALPHA
    }

    private fun changeTextSize(delta: Int) {
        val next = (textSizeSp + delta).coerceIn(MIN_TEXT_SIZE_SP, MAX_TEXT_SIZE_SP)
        if (next == textSizeSp) return
        textSizeSp = next
        applyTextSize()
        animateSample()
    }

    /** Подсвечивает надпись коротким увеличением, чтобы изменение было заметно. */
    private fun animateSample() {
        sampleText.animate()
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(90)
            .withEndAction {
                sampleText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }

    private fun pickBackground(colorRes: Int) {
        if (backgroundColorRes == colorRes) return
        backgroundColorRes = colorRes
        applyBackground()
    }

    /** Красит контейнер и строку состояния выбранным цветом палитры. */
    private fun applyBackground() {
        val color = getColor(backgroundColorRes)
        rootLayout.setBackgroundColor(color)
        window.statusBarColor = color
    }

    private companion object {
        const val DEFAULT_TEXT_SIZE_SP = 24
        const val MIN_TEXT_SIZE_SP = 16
        const val MAX_TEXT_SIZE_SP = 44
        const val STEP_SP = 4
        const val DISABLED_ALPHA = 0.4f
        const val KEY_TEXT_SIZE = "text_size_sp"
        const val KEY_BACKGROUND = "background_color"
    }
}
