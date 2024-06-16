package com.example.clickclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.clickclock.databinding.ActivityMainBinding
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private var dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private var timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private var isClockedIn = false
    private var time = 0.0

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPunchCard : PunchCard
    private lateinit var db : PunchCardDatabaseHelper
    private lateinit var punchInDateTime : LocalDateTime
    private lateinit var serviceIntent : Intent

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = onBackPressedDispatcher.addCallback(this) {
            minimizeApp()
        }
        callback.isEnabled = true

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED), RECEIVER_EXPORTED)

        db = PunchCardDatabaseHelper(this)

        binding.currentDateTextView.text = LocalDate.now().format(dateFormatter).toString()

        binding.openLogButton.setOnClickListener {
            val intent = Intent(this, LogViewActivity::class.java)
            startActivity(intent)
        }

        binding.clockInOutButton.setOnLongClickListener{
            // flip first for better readability
            isClockedIn = !isClockedIn
            if (isClockedIn)
            {
                startTimer()

                punchInDateTime = LocalDateTime.now()
                currentPunchCard = PunchCard(0, punchInDateTime.toLocalDate(), punchInDateTime.toLocalTime(), null, null)

                val clockedInSinceString = getString(R.string.clocked_in) + punchInDateTime.format(timeFormatter).toString()
                binding.clockedInOutTextView.text = clockedInSinceString
                binding.clockedInOutTextView.setTextColor(resources.getColor(R.color.green, theme))

                binding.clockedOutDurationTextView.text = ""

                binding.clockInOutButton.text = getString(R.string.clock_out)
                binding.clockInOutButton.setBackgroundColor(resources.getColor(R.color.red, theme))
                false
            }
            else
            {
                stopTimer()

                val punchOutDateTime = LocalDateTime.now()
                val duration = Duration.between(punchInDateTime, punchOutDateTime)

                val clockedOutAtString = getString(R.string.clocked_out) + punchOutDateTime.format(timeFormatter).toString()
                binding.clockedInOutTextView.text = clockedOutAtString
                binding.clockedInOutTextView.setTextColor(resources.getColor(R.color.red, theme))

                binding.clockedOutDurationTextView.text = makeDurationString(duration)

                currentPunchCard.punchOut = punchOutDateTime.toLocalTime()
                currentPunchCard.duration = duration

                binding.clockInOutButton.text = getString(R.string.clock_in)
                binding.clockInOutButton.setBackgroundColor(resources.getColor(R.color.green, theme))

                if (duration.toMinutes() >= 1.0)
                {
                    db.insertPunchCard(currentPunchCard)
                    Log.d("Punch Card Logged", currentPunchCard.toString())
                }

                false
            }
        }
    }

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        time = 0.0
        binding.timeCounterTextView.text = getTimeStringFromDouble(time)
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeCounterTextView.text = getTimeStringFromDouble(time)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String
    {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hours, min, sec)

    private fun makeDurationString(duration: Duration): String {
        return duration.toHours().toString() + " hours and " + (duration.toMinutes() % 60).toString() + " minutes"
    }

    private fun minimizeApp() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startMain)
    }
}