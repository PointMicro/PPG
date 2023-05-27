package com.example.ppg_test

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var ppgSensor: Sensor? = null
    private lateinit var heartRateTextView: TextView
    private lateinit var startButton: Button

    private var startTime: Long = 0
    private var endTime: Long = 0
    private var isMeasuring = false

    private val PERMISSION_REQUEST_BODY_SENSORS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        ppgSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        heartRateTextView = findViewById(R.id.heart_rate_text_view)
        startButton = findViewById(R.id.start_button)
        startButton.setOnClickListener {
            if (checkPermission()) {
                startMeasuring()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.BODY_SENSORS),
            PERMISSION_REQUEST_BODY_SENSORS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMeasuring()
            }
        }
    }

    private fun startMeasuring() {
        isMeasuring = true
        startTime = System.currentTimeMillis()
        sensorManager.registerListener(this, ppgSensor, SensorManager.SENSOR_DELAY_FASTEST)
        startButton.isEnabled = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isMeasuring) {
            val heartRate = event?.values?.get(0)?.toInt() ?: -1
            if (heartRate != -1) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - startTime >= 10000) {
                    isMeasuring = false
                    endTime = System.currentTimeMillis()
                    sensorManager.unregisterListener(this)
                    heartRateTextView.text = "Heart rate: $heartRate bpm"
                    startButton.isEnabled = true
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}
