package com.example.evidsos

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.sqrt

class ShakeEventListener(private val context: Context) : SensorEventListener {

    private val minForce = 10
    private val minDirectionChange = 3
    private val maxPauseBetweenDirectionChange = 200
    private val maxTotalDurationOfShake = 400

    private var firstDirectionChangeTime: Long = 0
    private var lastDirectionChangeTime: Long = 0
    private var directionChangeCount = 0

    private var lastMagnitude = 0f
    private var shakeListener: OnShakeListener? = null

    interface OnShakeListener {
        fun onShake()
    }

    fun setOnShakeListener(listener: OnShakeListener) {
        shakeListener = listener
    }

    override fun onSensorChanged(se: SensorEvent) {
        Log.d("ShakeEventListener", "onSensorChanged")
        val x = se.values[0]
        val y = se.values[1]
        val z = se.values[2]

        // Calculate the magnitude of the accelerometer vector
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val totalMovement = abs(magnitude - lastMagnitude)

        if (totalMovement > minForce) {
            Log.d("ShakeEventListener", "Total movement: $totalMovement")
            val now = System.currentTimeMillis()

            if (firstDirectionChangeTime == 0L) {
                firstDirectionChangeTime = now
                lastDirectionChangeTime = now
            }

            val lastChangeWasAgo = now - lastDirectionChangeTime
            if (lastChangeWasAgo < maxPauseBetweenDirectionChange) {
                lastDirectionChangeTime = now
                directionChangeCount++
                lastMagnitude = magnitude

                if (directionChangeCount >= minDirectionChange) {
                    val totalDuration = now - firstDirectionChangeTime
                    if (totalDuration < maxTotalDurationOfShake) {
                        Log.d("ShakeEventListener", "Shake detected!")
                        shakeListener?.onShake()

                        // Show a toast message
                        Toast.makeText(context, "Shake detected!", Toast.LENGTH_SHORT).show()
                        resetShakeParameters()
                    }
                }
            } else {
                resetShakeParameters()
            }
        }
    }

    private fun resetShakeParameters() {
        firstDirectionChangeTime = 0
        directionChangeCount = 0
        lastDirectionChangeTime = 0
        lastMagnitude = 0f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
