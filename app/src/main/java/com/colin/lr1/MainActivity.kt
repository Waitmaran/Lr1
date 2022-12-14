package com.colin.lr1

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.colin.lr1.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var alarmWorks : Boolean = false

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val message = intent.getStringExtra("Flux")
            binding.stepTextView.text = message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(
            mMessageReceiver, IntentFilter("ServiceMessage")
        );

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_DENIED)
                {
            //ask for permission
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.FOREGROUND_SERVICE), 0)
        }

        val intent = Intent(this@MainActivity, MyService::class.java)
        val pendingIntent : PendingIntent = PendingIntent.getService(this@MainActivity, 1, intent, FLAG_IMMUTABLE)
        val alarm = getSystemService(ALARM_SERVICE) as AlarmManager

        binding.buttonStart.setOnClickListener {
            if(!alarmWorks) {
                alarmWorks = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val request: PeriodicWorkRequest = PeriodicWorkRequest.Builder(BackupWorker::class.java,15, TimeUnit.MINUTES, 15, TimeUnit.SECONDS).addTag("BACKUP_WORKER_TAG").build()
                    WorkManager.getInstance(this).enqueue(request)
                } else
                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
            } else {
                Toast.makeText(this@MainActivity, "???????????? ?????? ????????????????!", Toast.LENGTH_SHORT).show()
            }
            //ContextCompat.startForegroundService(this@MainActivity, intent)
        }

        binding.buttonStop.setOnClickListener {
            if(alarmWorks) {
                //alarm.cancel(pendingIntent)
                alarmWorks = false
            } else {
                Toast.makeText(this@MainActivity, "???????????? ???? ??????????????!", Toast.LENGTH_SHORT).show()
            }

            //stopService(intent)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
    }
}

class BackupWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val intent = Intent(applicationContext, MyService::class.java)
        //val pendingIntent : PendingIntent = PendingIntent.getService(applicationContext, 1, intent, FLAG_IMMUTABLE)
        ContextCompat.startForegroundService(applicationContext, intent)
        return Result.success()
    }

    companion object {
        private const val TAG = "BackupWorker"
    }
}

class RestartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "onReceive")
        context.startService(Intent(context.applicationContext, MyService::class.java))
    }

    companion object {
        private const val TAG = "RestartServiceReceiver"
    }
}