package edu.temple.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var t: TimerThread

    var currentSeconds: Int = 0

    private var paused = false

    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning



        // Check if Timer is paused
        val paused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int){

            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                pause()
            }
        }

        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        Log.d("Current Time", getCurrentTime().toString())
        if (getCurrentTime() != null && getCurrentTime() != 0) {
            t = TimerThread(getCurrentTime())
        } else t = TimerThread(startValue)
        t.start()
    }

    fun pause () {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused
        }
    }

    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 0)  {
                    Log.d("Countdown", i.toString())

                    currentSeconds = i
                    saveCurrentTime()
                    timerHandler?.sendEmptyMessage(i)

                    while (paused);
                    sleep(1000)

                }
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("TimerService status", "Destroyed")
    }

    private fun saveCurrentTime() {
        val editor = sharedPreferences.edit()
        editor.putInt("currentSeconds", currentSeconds)
        editor.apply()
    }

    private fun getCurrentTime() : Int{
        return sharedPreferences.getInt("currentSeconds", 0)
    }


}