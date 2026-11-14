package ru.tulsu.mobileprogramming.notepad

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * Экран приложения «Блокнот».
 *
 * Все листы заметок обслуживает одно Activity: номер текущего листа хранится
 * в поле [sheetNumber], фон листа берётся из массива [SHEET_COLORS]. Текст
 * заметки и размер шрифта каждого листа сохраняются в SharedPreferences,
 * поэтому заметки не теряются ни при перелистывании, ни после перезапуска
 * приложения. Размер шрифта меняется кнопками «A−» и «A+» (задание для
 * самостоятельной работы).
 */
class MainActivity : Activity() {

    private lateinit var sheet: LinearLayout
    private lateinit var text: EditText
    private lateinit var next: Button
    private lateinit var sheetLabel: TextView
    private lateinit var fontValue: TextView
    private lateinit var fontDecrease: Button
    private lateinit var fontIncrease: Button

    private lateinit var prefs: SharedPreferences

    /** Номер текущего листа заметки. */
    private var sheetNumber = 0

    /** Текущий размер шрифта заметки в sp. */
    private var fontSizeSp = DEFAULT_FONT_SP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sheetNumber = prefs.getInt(KEY_SHEET, 0)
            .coerceIn(0, SHEET_COLORS.size - 1)
        showSheet()

        next.setOnClickListener { openNextSheet() }
        fontDecrease.setOnClickListener { changeFontSize(-FONT_STEP_SP) }
        fontIncrease.setOnClickListener { changeFontSize(FONT_STEP_SP) }
    }

    override fun onResume() {
        super.onResume()
        // Фон листа применяется при каждом выходе Activity на первый план:
        // при перелистывании Activity не пересоздаётся, поэтому цвет нужно
        // выставлять заново.
        sheet.setBackgroundColor(getColor(SHEET_COLORS[sheetNumber]))
    }

    override fun onPause() {
        super.onPause()
        saveCurrentNote()
    }

    private fun bindViews() {
        sheet = findViewById(R.id.sheet)
        text = findViewById(R.id.text)
        next = findViewById(R.id.next)
        sheetLabel = findViewById(R.id.sheetLabel)
        fontValue = findViewById(R.id.fontValue)
        fontDecrease = findViewById(R.id.fontDecrease)
        fontIncrease = findViewById(R.id.fontIncrease)
    }

    /** Показывает лист: фон, номер, заметку и размер шрифта. */
    private fun showSheet() {
        sheet.setBackgroundColor(getColor(SHEET_COLORS[sheetNumber]))
        sheetLabel.text = getString(
            R.string.sheet_counter, sheetNumber + 1, SHEET_COLORS.size,
        )
        text.setText(prefs.getString(KEY_NOTE + sheetNumber, "").orEmpty())
        val saved = prefs.getInt(KEY_FONT + sheetNumber, DEFAULT_FONT_SP)
        fontSizeSp = saved.coerceIn(MIN_FONT_SP, MAX_FONT_SP)
        applyFontSize()
    }

    /** Записывает заметку текущего листа в SharedPreferences. */
    private fun saveCurrentNote() {
        val note = text.text.toString()
        prefs.edit().putString(KEY_NOTE + sheetNumber, note).apply()
    }

    /** Сохраняет текущую заметку и открывает следующий лист по кругу. */
    private fun openNextSheet() {
        saveCurrentNote()
        sheetNumber = (sheetNumber + 1) % SHEET_COLORS.size
        prefs.edit().putInt(KEY_SHEET, sheetNumber).apply()
        showSheet()
    }

    /** Меняет размер шрифта заметки и сохраняет его для текущего листа. */
    private fun changeFontSize(delta: Int) {
        val updated = (fontSizeSp + delta).coerceIn(MIN_FONT_SP, MAX_FONT_SP)
        if (updated == fontSizeSp) {
            val messageRes = if (delta < 0) {
                R.string.font_minimum
            } else {
                R.string.font_maximum
            }
            val message = getString(messageRes, fontSizeSp)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }
        fontSizeSp = updated
        applyFontSize()
        prefs.edit().putInt(KEY_FONT + sheetNumber, fontSizeSp).apply()
    }

    /** Применяет размер шрифта к полю заметки и к подписи со значением. */
    private fun applyFontSize() {
        text.textSize = fontSizeSp.toFloat()
        fontValue.text = getString(R.string.font_value, fontSizeSp)
    }

    companion object {
        /** Имя файла настроек, в котором хранятся заметки. */
        private const val PREFS_NAME = "notepad"

        /** Ключ с номером последнего открытого листа. */
        private const val KEY_SHEET = "sheetNumber"

        /** Ключ заметки листа: к префиксу дописывается номер листа. */
        private const val KEY_NOTE = "note"

        /** Ключ размера шрифта листа: к префиксу дописывается номер. */
        private const val KEY_FONT = "font"

        private const val DEFAULT_FONT_SP = 16
        private const val MIN_FONT_SP = 12
        private const val MAX_FONT_SP = 32
        private const val FONT_STEP_SP = 4

        /**
         * Фоны листов заметок. Длина массива задаёт количество листов:
         * чтобы добавить лист, достаточно дописать сюда ещё один цвет.
         */
        private val SHEET_COLORS = intArrayOf(
            R.color.sheet_1, R.color.sheet_2, R.color.sheet_3,
            R.color.sheet_4, R.color.sheet_5, R.color.sheet_6,
        )
    }
}
