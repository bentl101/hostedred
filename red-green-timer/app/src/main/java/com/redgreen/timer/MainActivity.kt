package com.redgreen.timer

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private enum class Mode { SETUP, RUNNING, DONE }

    private val presets = intArrayOf(1, 2, 3, 5, 10, 15)

    private lateinit var setupScreen: ScrollView
    private lateinit var timerScreen: LinearLayout
    private lateinit var presetGrid: GridLayout
    private lateinit var minutesLabel: TextView
    private lateinit var statusLabel: TextView
    private lateinit var timeLabel: TextView
    private lateinit var hintLabel: TextView
    private lateinit var pie: PieTimerView
    private lateinit var soundSwitch: Switch
    private lateinit var vibrateSwitch: Switch
    private lateinit var numbersSwitch: Switch

    private val prefs by lazy { getSharedPreferences("settings", Context.MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())

    private var mode = Mode.SETUP
    private var minutes = 5
    private var totalMs = 0L
    /** Finish time on the elapsedRealtime clock, so the timer is accurate even if the app is paused. */
    private var endAt = 0L

    private val tick = object : Runnable {
        override fun run() {
            updateRunning()
            if (mode == Mode.RUNNING) handler.postDelayed(this, 100)
        }
    }

    private val holdToStop = Runnable { showSetup() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupScreen = findViewById(R.id.setupScreen)
        timerScreen = findViewById(R.id.timerScreen)
        presetGrid = findViewById(R.id.presetGrid)
        minutesLabel = findViewById(R.id.minutesLabel)
        statusLabel = findViewById(R.id.statusLabel)
        timeLabel = findViewById(R.id.timeLabel)
        hintLabel = findViewById(R.id.hintLabel)
        pie = findViewById(R.id.pie)
        soundSwitch = findViewById(R.id.soundSwitch)
        vibrateSwitch = findViewById(R.id.vibrateSwitch)
        numbersSwitch = findViewById(R.id.numbersSwitch)

        minutes = prefs.getInt("minutes", 5)
        soundSwitch.isChecked = prefs.getBoolean("sound", true)
        vibrateSwitch.isChecked = prefs.getBoolean("vibrate", true)
        numbersSwitch.isChecked = prefs.getBoolean("numbers", true)
        soundSwitch.setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean("sound", on).apply() }
        vibrateSwitch.setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean("vibrate", on).apply() }
        numbersSwitch.setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean("numbers", on).apply() }

        buildPresetButtons()
        findViewById<Button>(R.id.minusButton).setOnClickListener { setMinutes(minutes - 1) }
        findViewById<Button>(R.id.plusButton).setOnClickListener { setMinutes(minutes + 1) }
        findViewById<Button>(R.id.startButton).setOnClickListener { start(minutes * 60_000L) }

        setUpTimerScreenTouch()

        if (savedInstanceState != null) {
            totalMs = savedInstanceState.getLong("totalMs")
            endAt = savedInstanceState.getLong("endAt")
            mode = Mode.valueOf(savedInstanceState.getString("mode", Mode.SETUP.name))
        }
        when (mode) {
            Mode.SETUP -> showSetup()
            Mode.RUNNING -> showRunning()
            Mode.DONE -> showDone(celebrate = false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("mode", mode.name)
        outState.putLong("totalMs", totalMs)
        outState.putLong("endAt", endAt)
    }

    override fun onResume() {
        super.onResume()
        if (mode == Mode.RUNNING) handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    // ---------- Setup ----------

    private fun buildPresetButtons() {
        val size = (96 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()
        for (p in presets) {
            val b = Button(this).apply {
                text = "$p\n${getString(R.string.minutes_short)}"
                textSize = 24f
                isAllCaps = false
                setTextColor(getColor(R.color.white))
                setBackgroundResource(R.drawable.rounded_button)
                tag = p
                setOnClickListener { setMinutes(p) }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(margin, margin, margin, margin)
            }
            presetGrid.addView(b, lp)
        }
        setMinutes(minutes)
    }

    private fun setMinutes(value: Int) {
        minutes = value.coerceIn(1, 120)
        prefs.edit().putInt("minutes", minutes).apply()
        minutesLabel.text = "$minutes ${getString(R.string.minutes_short)}"
        for (i in 0 until presetGrid.childCount) {
            val child = presetGrid.getChildAt(i)
            child.isSelected = child.tag == minutes
        }
    }

    private fun showSetup() {
        mode = Mode.SETUP
        handler.removeCallbacks(tick)
        setupScreen.visibility = View.VISIBLE
        timerScreen.visibility = View.GONE
        setBars(R.color.setup_bg)
    }

    // ---------- Running (red) ----------

    private fun start(durationMs: Long) {
        totalMs = durationMs
        endAt = SystemClock.elapsedRealtime() + durationMs
        mode = Mode.RUNNING
        showRunning()
    }

    private fun showRunning() {
        setupScreen.visibility = View.GONE
        timerScreen.visibility = View.VISIBLE
        timerScreen.setBackgroundColor(getColor(R.color.timer_red))
        setBars(R.color.timer_red)
        statusLabel.text = getString(R.string.wait)
        hintLabel.text = getString(R.string.hold_to_stop)
        pie.finished = false
        timeLabel.visibility = if (numbersSwitch.isChecked) View.VISIBLE else View.INVISIBLE
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    private fun updateRunning() {
        if (mode != Mode.RUNNING) return
        val left = endAt - SystemClock.elapsedRealtime()
        if (left <= 0) {
            showDone(celebrate = true)
            return
        }
        pie.fractionLeft = left.toFloat() / totalMs
        val secs = (left + 999) / 1000
        timeLabel.text = String.format(java.util.Locale.US, "%d:%02d", secs / 60, secs % 60)
    }

    // ---------- Done (green) ----------

    private fun showDone(celebrate: Boolean) {
        mode = Mode.DONE
        handler.removeCallbacks(tick)
        setupScreen.visibility = View.GONE
        timerScreen.visibility = View.VISIBLE
        timerScreen.setBackgroundColor(getColor(R.color.timer_green))
        setBars(R.color.timer_green)
        statusLabel.text = getString(R.string.done)
        timeLabel.visibility = View.INVISIBLE
        hintLabel.text = getString(R.string.tap_to_reset)
        pie.fractionLeft = 0f
        pie.finished = true
        if (celebrate) {
            if (soundSwitch.isChecked) playChime()
            if (vibrateSwitch.isChecked) vibrate()
        }
    }

    private fun playChime() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 600)
            handler.postDelayed({ tone.release() }, 1000)
        } catch (_: RuntimeException) {
            // Some devices can't create a ToneGenerator; the green screen is the main signal anyway.
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1))
    }

    // ---------- Touch handling ----------

    /**
     * While RED: a child tapping the screen does nothing. A grown-up must press and hold
     * for 2 seconds to cancel. While GREEN: one tap goes back to the setup screen.
     */
    private fun setUpTimerScreenTouch() {
        timerScreen.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (mode == Mode.RUNNING) handler.postDelayed(holdToStop, 2000)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(holdToStop)
                    if (mode == Mode.DONE) {
                        v.performClick()
                        showSetup()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(holdToStop)
                    true
                }
                else -> true
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Don't let the back button accidentally cancel a running timer.
        if (mode == Mode.RUNNING) return
        if (mode == Mode.DONE) {
            showSetup()
            return
        }
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    private fun setBars(color: Int) {
        val c = getColor(color)
        @Suppress("DEPRECATION")
        window.statusBarColor = c
        @Suppress("DEPRECATION")
        window.navigationBarColor = c
    }
}
