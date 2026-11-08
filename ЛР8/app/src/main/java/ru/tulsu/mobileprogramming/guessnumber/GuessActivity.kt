package ru.tulsu.mobileprogramming.guessnumber

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.random.Random

/**
 * Второй экран приложения «Угадай число».
 *
 * Получает из Intent границы диапазона, загадывает случайное число и предлагает
 * пользователю угадать его. После каждой попытки экран подсказывает, больше или
 * меньше загаданное число, и показывает оставшийся диапазон. Результат игры
 * (загаданное число, число попыток и признак победы) возвращается на первый экран
 * через setResult.
 */
class GuessActivity : Activity() {

    private lateinit var subtitleView: TextView
    private lateinit var attemptView: TextView
    private lateinit var rangeView: TextView
    private lateinit var hintView: TextView
    private lateinit var guessInput: EditText
    private lateinit var checkButton: Button
    private lateinit var surrenderButton: Button

    private var minValue = DEFAULT_MIN
    private var maxValue = DEFAULT_MAX
    private var target = DEFAULT_MIN
    private var attempts = 0
    private var currentLow = DEFAULT_MIN
    private var currentHigh = DEFAULT_MAX
    private var finished = false
    private var won = false
    private var hintMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess)

        subtitleView = findViewById(R.id.guessSubtitle)
        attemptView = findViewById(R.id.attemptView)
        rangeView = findViewById(R.id.rangeView)
        hintView = findViewById(R.id.hintView)
        guessInput = findViewById(R.id.guessInput)
        checkButton = findViewById(R.id.checkButton)
        surrenderButton = findViewById(R.id.surrenderButton)

        minValue = intent.getIntExtra(EXTRA_MIN, DEFAULT_MIN)
        maxValue = intent.getIntExtra(EXTRA_MAX, DEFAULT_MAX)
        if (maxValue <= minValue) maxValue = minValue + 1

        if (savedInstanceState == null) {
            target = randomTarget()
            currentLow = minValue
            currentHigh = maxValue
        } else {
            target = savedInstanceState.getInt(KEY_TARGET, minValue)
            attempts = savedInstanceState.getInt(KEY_ATTEMPTS, 0)
            currentLow = savedInstanceState.getInt(KEY_LOW, minValue)
            currentHigh = savedInstanceState.getInt(KEY_HIGH, maxValue)
            finished = savedInstanceState.getBoolean(KEY_FINISHED, false)
            won = savedInstanceState.getBoolean(KEY_WON, false)
            hintMessage = savedInstanceState.getString(KEY_HINT)
        }

        subtitleView.text = getString(R.string.guess_subtitle, minValue, maxValue)
        checkButton.setOnClickListener { onCheckClicked() }
        surrenderButton.setOnClickListener { surrender() }
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
        guessInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onCheckClicked()
                true
            } else {
                false
            }
        }

        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_TARGET, target)
        outState.putInt(KEY_ATTEMPTS, attempts)
        outState.putInt(KEY_LOW, currentLow)
        outState.putInt(KEY_HIGH, currentHigh)
        outState.putBoolean(KEY_FINISHED, finished)
        outState.putBoolean(KEY_WON, won)
        outState.putString(KEY_HINT, hintMessage)
        super.onSaveInstanceState(outState)
    }

    /** Обрабатывает нажатие «Проверить» либо начинает новую игру после финала. */
    private fun onCheckClicked() {
        if (finished) {
            startNewGame()
            return
        }
        val text = guessInput.text.toString().trim()
        val guess = text.toIntOrNull()
        if (text.isEmpty() || guess == null) {
            hintMessage = getString(R.string.error_empty)
            render()
            return
        }
        if (guess < currentLow || guess > currentHigh) {
            hintMessage = getString(R.string.error_out_of_range)
            render()
            return
        }

        attempts++
        when {
            guess < target -> {
                currentLow = guess + 1
                hintMessage = getString(R.string.hint_greater)
            }
            guess > target -> {
                currentHigh = guess - 1
                hintMessage = getString(R.string.hint_less)
            }
            else -> {
                finished = true
                won = true
                hintMessage = getString(R.string.hint_won, attempts)
                sendResult()
            }
        }
        guessInput.setText("")
        render()
    }

    /** Завершает игру без победы и показывает загаданное число. */
    private fun surrender() {
        if (finished) return
        finished = true
        won = false
        hintMessage = getString(R.string.surrender_text, target)
        sendResult()
        render()
    }

    private fun startNewGame() {
        target = randomTarget()
        attempts = 0
        currentLow = minValue
        currentHigh = maxValue
        finished = false
        won = false
        hintMessage = null
        guessInput.setText("")
        setResult(RESULT_CANCELED)
        render()
    }

    private fun randomTarget(): Int = Random.nextInt(minValue, maxValue + 1)

    /** Возвращает результат игры на первый экран. */
    private fun sendResult() {
        val data = Intent().apply {
            putExtra(EXTRA_TARGET, target)
            putExtra(EXTRA_ATTEMPTS, attempts)
            putExtra(EXTRA_WON, won)
        }
        setResult(RESULT_OK, data)
    }

    private fun render() {
        attemptView.text = getString(R.string.guess_attempt, attempts)
        rangeView.visibility = if (finished) View.GONE else View.VISIBLE
        rangeView.text = getString(R.string.hint_range, currentLow, currentHigh)

        val hint = hintMessage
        hintView.visibility = if (hint != null) View.VISIBLE else View.GONE
        hintView.text = hint ?: ""

        checkButton.text = getString(if (finished) R.string.button_new_game else R.string.button_check)
        surrenderButton.visibility = if (finished) View.GONE else View.VISIBLE
        guessInput.isEnabled = !finished
    }

    companion object {
        const val EXTRA_MIN = "ru.tulsu.mobileprogramming.guessnumber.extra.MIN"
        const val EXTRA_MAX = "ru.tulsu.mobileprogramming.guessnumber.extra.MAX"
        const val EXTRA_TARGET = "ru.tulsu.mobileprogramming.guessnumber.extra.TARGET"
        const val EXTRA_ATTEMPTS = "ru.tulsu.mobileprogramming.guessnumber.extra.ATTEMPTS"
        const val EXTRA_WON = "ru.tulsu.mobileprogramming.guessnumber.extra.WON"

        private const val DEFAULT_MIN = 1
        private const val DEFAULT_MAX = 100
        private const val KEY_TARGET = "target"
        private const val KEY_ATTEMPTS = "attempts"
        private const val KEY_LOW = "current_low"
        private const val KEY_HIGH = "current_high"
        private const val KEY_FINISHED = "finished"
        private const val KEY_WON = "won"
        private const val KEY_HINT = "hint_message"
    }
}
