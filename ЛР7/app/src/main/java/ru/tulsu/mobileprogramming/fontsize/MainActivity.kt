package ru.tulsu.mobileprogramming.fontsize

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

/**
 * Экран изменения размера шрифта надписи и цвета фона.
 *
 * Размер надписи меняется кнопками «Мельче» и «Крупнее» в диапазоне
 * MIN_TEXT_SIZE_SP..MAX_TEXT_SIZE_SP, кнопка «Сброс» возвращает исходное
 * значение. Кнопки палитры перекрашивают фон: жёлтый применяется сразу,
 * а красный и зелёный запускают плавную анимацию к чистому цвету. После
 * нажатия фон сам постепенно краснеет (зеленеет): «свой» канал растёт,
 * остальные убывают, пока не достигнут максимума. Текущий размер и цвет
 * фона сохраняются при пересоздании Activity.
 */
class MainActivity : Activity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var sampleText: TextView
    private lateinit var sizeValue: TextView
    private lateinit var smallerButton: Button
    private lateinit var largerButton: Button

    private var textSizeSp = DEFAULT_TEXT_SIZE_SP
    private var backgroundColor = 0
    private var backgroundAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.root_layout)
        sampleText = findViewById(R.id.sampleText)
        sizeValue = findViewById(R.id.sizeValue)
        smallerButton = findViewById(R.id.smallerButton)
        largerButton = findViewById(R.id.largerButton)

        val defaultColor = getColor(R.color.redColor)
        backgroundColor = savedInstanceState?.getInt(KEY_BACKGROUND, defaultColor) ?: defaultColor

        applyTextSize()
        applyBackground()

        smallerButton.setOnClickListener { changeTextSize(-STEP_SP) }
        largerButton.setOnClickListener { changeTextSize(STEP_SP) }
        findViewById<Button>(R.id.resetButton).setOnClickListener {
            textSizeSp = DEFAULT_TEXT_SIZE_SP
            applyTextSize()
        }

        findViewById<Button>(R.id.redButton).setOnClickListener { animateBackgroundTo(Color.RED) }
        findViewById<Button>(R.id.yellowButton).setOnClickListener { applyPaletteColor(R.color.yellowColor) }
        findViewById<Button>(R.id.greenButton).setOnClickListener { animateBackgroundTo(Color.GREEN) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_TEXT_SIZE, textSizeSp)
        outState.putInt(KEY_BACKGROUND, backgroundColor)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        backgroundAnimator?.cancel()
        super.onDestroy()
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

    /** Жёлтый цвет, как и раньше, применяется к фону сразу, без ступеней. */
    private fun applyPaletteColor(colorRes: Int) {
        backgroundAnimator?.cancel()
        backgroundColor = getColor(colorRes)
        applyBackground()
    }

    /**
     * Плавно ведёт фон к чистому цвету кнопки за COLOR_TRANSITION_MS.
     *
     * У красного цвета красный канал растёт, зелёный и синий убывают;
     * у зелёного — наоборот. Аниматор сам проходит все промежуточные
     * оттенки и останавливается, когда каналы упрутся в максимум.
     * Повторное нажатие во время анимации плавно перенаправляет фон
     * к новому цвету с текущего оттенка.
     */
    private fun animateBackgroundTo(targetColor: Int) {
        val from = backgroundColor
        if (from == targetColor) return

        backgroundAnimator?.cancel()
        backgroundAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = COLOR_TRANSITION_MS
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                backgroundColor = blendColors(from, targetColor, animator.animatedFraction)
                applyBackground()
            }
            start()
        }
    }

    /** Промежуточный цвет для кадров анимации. */
    private fun blendColors(from: Int, to: Int, fraction: Float): Int = Color.rgb(
        blendChannel(Color.red(from), Color.red(to), fraction),
        blendChannel(Color.green(from), Color.green(to), fraction),
        blendChannel(Color.blue(from), Color.blue(to), fraction)
    )

    private fun blendChannel(from: Int, to: Int, fraction: Float): Int =
        (from + (to - from) * fraction).roundToInt().coerceIn(0, 255)

    /** Красит контейнер и строку состояния текущим цветом фона. */
    private fun applyBackground() {
        rootLayout.setBackgroundColor(backgroundColor)
        window.statusBarColor = backgroundColor
    }

    private companion object {
        const val DEFAULT_TEXT_SIZE_SP = 24
        const val MIN_TEXT_SIZE_SP = 16
        const val MAX_TEXT_SIZE_SP = 44
        const val STEP_SP = 4
        const val DISABLED_ALPHA = 0.4f

        /** Длительность автоматического перехода фона к чистому цвету. */
        const val COLOR_TRANSITION_MS = 1500L

        const val KEY_TEXT_SIZE = "text_size_sp"
        const val KEY_BACKGROUND = "background_color"
    }
}
