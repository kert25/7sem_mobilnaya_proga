package ru.tulsu.mobileprogramming.guessnumber

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Первый экран приложения «Угадай число».
 *
 * Пользователь задаёт диапазон и загадывает число из него, после чего компьютер
 * отгадывает задуманное делением диапазона пополам: на каждом шаге показывается
 * текущая догадка, а кнопки «Меньше» и «Больше» сужают диапазон. Кнопка
 * «Теперь я угадаю» открывает второе Activity и передаёт ему границы диапазона
 * через объект Intent.
 */
class MainActivity : Activity() {

    private lateinit var minInput: EditText
    private lateinit var maxInput: EditText
    private lateinit var rangeError: TextView
    private lateinit var startButton: Button
    private lateinit var guessPanel: LinearLayout
    private lateinit var attemptView: TextView
    private lateinit var questionView: TextView
    private lateinit var lessButton: Button
    private lateinit var greaterButton: Button
    private lateinit var correctButton: Button
    private lateinit var resultView: TextView
    private lateinit var restartButton: Button
    private lateinit var openGuessButton: Button
    private lateinit var secondScreenResult: TextView

    private var minValue = DEFAULT_MIN
    private var maxValue = DEFAULT_MAX
    private var low = DEFAULT_MIN
    private var high = DEFAULT_MAX
    private var currentGuess = 0
    private var attempts = 0
    private var gameActive = false
    private var gameFinished = false
    private var resultMessage: String? = null
    private var secondScreenMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        restoreState(savedInstanceState)

        startButton.setOnClickListener { startGame() }
        lessButton.setOnClickListener { narrowRangeToLowerHalf() }
        greaterButton.setOnClickListener { narrowRangeToUpperHalf() }
        correctButton.setOnClickListener { confirmGuess() }
        restartButton.setOnClickListener { resetGame() }
        openGuessButton.setOnClickListener { openGuessActivity() }

        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_MIN, minValue)
        outState.putInt(KEY_MAX, maxValue)
        outState.putInt(KEY_LOW, low)
        outState.putInt(KEY_HIGH, high)
        outState.putInt(KEY_GUESS, currentGuess)
        outState.putInt(KEY_ATTEMPTS, attempts)
        outState.putBoolean(KEY_ACTIVE, gameActive)
        outState.putBoolean(KEY_FINISHED, gameFinished)
        outState.putString(KEY_RESULT, resultMessage)
        outState.putString(KEY_SECOND_RESULT, secondScreenMessage)
        super.onSaveInstanceState(outState)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_GUESS) return
        secondScreenMessage = if (resultCode == RESULT_OK && data != null) {
            val target = data.getIntExtra(GuessActivity.EXTRA_TARGET, 0)
            val tries = data.getIntExtra(GuessActivity.EXTRA_ATTEMPTS, 0)
            if (data.getBooleanExtra(GuessActivity.EXTRA_WON, false)) {
                getString(R.string.second_screen_result_won, target, tries)
            } else {
                getString(R.string.second_screen_result_lost, target)
            }
        } else {
            getString(R.string.second_screen_result_cancelled)
        }
        render()
    }

    private fun bindViews() {
        minInput = findViewById(R.id.minInput)
        maxInput = findViewById(R.id.maxInput)
        rangeError = findViewById(R.id.rangeError)
        startButton = findViewById(R.id.startButton)
        guessPanel = findViewById(R.id.guessPanel)
        attemptView = findViewById(R.id.attemptView)
        questionView = findViewById(R.id.questionView)
        lessButton = findViewById(R.id.lessButton)
        greaterButton = findViewById(R.id.greaterButton)
        correctButton = findViewById(R.id.correctButton)
        resultView = findViewById(R.id.resultView)
        restartButton = findViewById(R.id.restartButton)
        openGuessButton = findViewById(R.id.openGuessButton)
        secondScreenResult = findViewById(R.id.secondScreenResult)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        minValue = savedInstanceState.getInt(KEY_MIN, DEFAULT_MIN)
        maxValue = savedInstanceState.getInt(KEY_MAX, DEFAULT_MAX)
        low = savedInstanceState.getInt(KEY_LOW, minValue)
        high = savedInstanceState.getInt(KEY_HIGH, maxValue)
        currentGuess = savedInstanceState.getInt(KEY_GUESS, 0)
        attempts = savedInstanceState.getInt(KEY_ATTEMPTS, 0)
        gameActive = savedInstanceState.getBoolean(KEY_ACTIVE, false)
        gameFinished = savedInstanceState.getBoolean(KEY_FINISHED, false)
        resultMessage = savedInstanceState.getString(KEY_RESULT)
        secondScreenMessage = savedInstanceState.getString(KEY_SECOND_RESULT)
        minInput.setText(minValue.toString())
        maxInput.setText(maxValue.toString())
    }

    /** Проверяет введённые границы и возвращает диапазон либо показывает ошибку. */
    private fun readRange(): Pair<Int, Int>? {
        val minText = minInput.text.toString().trim()
        val maxText = maxInput.text.toString().trim()
        if (minText.isEmpty() || maxText.isEmpty()) {
            showRangeError(getString(R.string.range_error_empty))
            return null
        }
        val min = minText.toIntOrNull()
        val max = maxText.toIntOrNull()
        if (min == null || max == null) {
            showRangeError(getString(R.string.range_error_invalid))
            return null
        }
        if (max <= min) {
            showRangeError(getString(R.string.range_error_order))
            return null
        }
        if (max - min + 1 > MAX_RANGE_SIZE) {
            showRangeError(getString(R.string.range_error_big, MAX_RANGE_SIZE))
            return null
        }
        rangeError.visibility = View.GONE
        return min to max
    }

    private fun showRangeError(message: String) {
        rangeError.text = message
        rangeError.visibility = View.VISIBLE
    }

    /** Начинает игру: компьютер делает первую догадку в середине диапазона. */
    private fun startGame() {
        val range = readRange() ?: return
        minValue = range.first
        maxValue = range.second
        low = minValue
        high = maxValue
        currentGuess = (low + high) / 2
        attempts = 1
        gameActive = true
        gameFinished = false
        resultMessage = null
        render()
    }

    /** Ответ «Меньше»: загаданное число находится ниже текущей догадки. */
    private fun narrowRangeToLowerHalf() {
        if (!gameActive || gameFinished) return
        high = currentGuess - 1
        makeNextGuess()
    }

    /** Ответ «Больше»: загаданное число находится выше текущей догадки. */
    private fun narrowRangeToUpperHalf() {
        if (!gameActive || gameFinished) return
        low = currentGuess + 1
        makeNextGuess()
    }

    private fun makeNextGuess() {
        if (low > high) {
            gameActive = false
            gameFinished = true
            resultMessage = getString(R.string.result_contradiction)
            render()
            return
        }
        currentGuess = (low + high) / 2
        attempts++
        render()
    }

    private fun confirmGuess() {
        if (!gameActive || gameFinished) return
        gameActive = false
        gameFinished = true
        resultMessage = getString(R.string.result_won, currentGuess, attempts)
        render()
    }

    private fun resetGame() {
        gameActive = false
        gameFinished = false
        resultMessage = null
        currentGuess = 0
        attempts = 0
        render()
    }

    /** Открывает второе Activity, передавая ему границы диапазона. */
    private fun openGuessActivity() {
        val range = readRange() ?: return
        minValue = range.first
        maxValue = range.second
        val intent = Intent(this, GuessActivity::class.java).apply {
            putExtra(GuessActivity.EXTRA_MIN, minValue)
            putExtra(GuessActivity.EXTRA_MAX, maxValue)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_GUESS)
    }

    /** Число попыток, которое гарантирует деление диапазона пополам. */
    private fun maxAttempts(): Int {
        var size = maxValue - minValue + 1
        var count = 0
        while (size > 1) {
            size = (size + 1) / 2
            count++
        }
        return count
    }

    private fun render() {
        minInput.isEnabled = !gameActive
        maxInput.isEnabled = !gameActive
        startButton.isEnabled = !gameActive
        startButton.alpha = if (gameActive) DISABLED_ALPHA else 1f

        val guessing = gameActive && !gameFinished
        guessPanel.visibility = if (guessing) View.VISIBLE else View.GONE
        if (guessing) {
            attemptView.text = getString(R.string.attempt_counter, attempts, maxAttempts())
            questionView.text = getString(R.string.guess_question, currentGuess)
        }

        restartButton.visibility = if (gameFinished) View.VISIBLE else View.GONE

        val message = resultMessage
        resultView.visibility = if (message != null) View.VISIBLE else View.GONE
        resultView.text = message ?: ""

        val secondMessage = secondScreenMessage
        secondScreenResult.visibility = if (secondMessage != null) View.VISIBLE else View.GONE
        secondScreenResult.text = secondMessage ?: ""
    }

    private companion object {
        const val DEFAULT_MIN = 1
        const val DEFAULT_MAX = 100
        const val MAX_RANGE_SIZE = 10_000
        const val DISABLED_ALPHA = 0.5f
        const val REQUEST_GUESS = 100
        const val KEY_MIN = "min_value"
        const val KEY_MAX = "max_value"
        const val KEY_LOW = "low"
        const val KEY_HIGH = "high"
        const val KEY_GUESS = "current_guess"
        const val KEY_ATTEMPTS = "attempts"
        const val KEY_ACTIVE = "game_active"
        const val KEY_FINISHED = "game_finished"
        const val KEY_RESULT = "result_message"
        const val KEY_SECOND_RESULT = "second_screen_message"
    }
}
