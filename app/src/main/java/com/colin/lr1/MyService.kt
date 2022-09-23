package com.colin.lr1

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.colin.lr1.databinding.ActivityMainBinding
import java.util.function.Consumer

class MyService : Service() {
    private var sensor: Sensor? = null
    private var s: SensorManager? = null
    private var channel : NotificationChannel? = null
    private var sensorValue: String = "No Data"
    private var notificationManager: NotificationManager? = null



    private val listener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            if(p0?.sensor?.type == Sensor.TYPE_LIGHT) {
                val flux : String = p0.values?.get(0).toString()
                sensorValue = flux;
                //Log.d("FLUX", flux)
                val intent : Intent = Intent("ServiceMessage")
                intent.putExtra("Flux", sensorValue)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                val pendingMainActivityIntent: PendingIntent =
                    Intent(applicationContext, MainActivity::class.java).let { notificationIntent ->
                        PendingIntent.getActivity(applicationContext, 0, notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE)
                    }

                val notification: Notification = Notification.Builder(applicationContext, "MyApp")
                    .setContentTitle("Измерение")
                    .setContentText("Уровень освещенности: $sensorValue")
                    .setSmallIcon(androidx.constraintlayout.widget.R.drawable.notification_template_icon_bg)
                    .setContentIntent(pendingMainActivityIntent)
                    .setTicker("1")
                    .build()
                notificationManager!!.notify(2,notification)
                stopForeground(false)
                stopSelf()
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

        }

    }
    override fun onCreate() {
        super.onCreate()
        s = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = if(s?.getSensorList(Sensor.TYPE_LIGHT)?.size!! >0) s?.getDefaultSensor(Sensor.TYPE_LIGHT)
        else null
        s?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        /*sensor?.let { s?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        s?.getSensorList(Sensor.TYPE_ALL)?.forEach(Consumer { x: Sensor ->
            Log.d(
                "SENSOR", x.stringType,
            )
        })*/
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("SERVICE", " STARTED")
        channel = NotificationChannel("MyApp", "ServiceNotify", NotificationManager.IMPORTANCE_DEFAULT)
        channel!!.description = "MyApp notification Channel"

        val pendingMainActivityIntent: PendingIntent =
            Intent(applicationContext, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(applicationContext, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.createNotificationChannel(channel!!)
        val notification: Notification = Notification.Builder(applicationContext, "MyApp")
            .setContentTitle("Сервис запущен")
            .setContentText("Измерение освещенности")
            .setSmallIcon(androidx.constraintlayout.widget.R.drawable.notification_template_icon_bg)
            .setContentIntent(pendingMainActivityIntent)
            .setTicker("0")
            .build()
        Log.d("SERVICE", "NOTIFICATION CREATED")
        startForeground(1, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d("SERVICE", " STOPPED")
        s?.unregisterListener(listener)
        //stopForeground(false)
        //stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}