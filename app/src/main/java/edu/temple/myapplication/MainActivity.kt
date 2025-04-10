package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerService: TimerService? = null
    private var isBound = false
    private lateinit var textView: TextView
    private lateinit var timerBinder: TimerService.TimerBinder

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(timerHandler)
            isBound = true
        }

        private val timerHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                var seconds = msg.what
                super.handleMessage(msg)
                val formattedTime = String.format("%02d:%02d", seconds / 60, seconds % 60)
                textView.text = formattedTime
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_start -> {
                timerBinder.start(10)
                true
            }

            R.id.action_stop -> {
                timerBinder.stop()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }
}