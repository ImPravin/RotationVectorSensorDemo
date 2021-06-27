package com.sample.mylibrary

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import android.view.WindowManager

class RotationVectorListener private constructor(context: Context) : SensorEventListener {

    companion object : SingletonHolder<RotationVectorListener, Context>(::RotationVectorListener)

    private var windowManager: WindowManager? = null
    private var mSensorManager: SensorManager? = null
    private var deviceData:DeviceData? = null

    init {
        deviceData = DeviceData()
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun getDeviceData(): DeviceData? {
        return deviceData
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR -> processSensorOrientation(
                event.values)
            else -> Log.e("DeviceOrientation", "Sensor event type not supported")
        }
    }

    private fun processSensorOrientation(rotation: FloatArray) {
        val rotationMatrix = FloatArray(16)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotation)
        val worldAxisX: Int
        val worldAxisY: Int
        when (windowManager?.defaultDisplay?.rotation) {
            Surface.ROTATION_90 -> {
                worldAxisX = SensorManager.AXIS_Z
                worldAxisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                worldAxisX = SensorManager.AXIS_MINUS_X
                worldAxisY = SensorManager.AXIS_MINUS_Z
            }
            Surface.ROTATION_270 -> {
                worldAxisX = SensorManager.AXIS_MINUS_Z
                worldAxisY = SensorManager.AXIS_X
            }
            Surface.ROTATION_0 -> {
                worldAxisX = SensorManager.AXIS_X
                worldAxisY = SensorManager.AXIS_Z
            }
            else -> {
                worldAxisX = SensorManager.AXIS_X
                worldAxisY = SensorManager.AXIS_Z
            }
        }
        val adjustedRotationMatrix = FloatArray(16)
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX,
            worldAxisY, adjustedRotationMatrix)

        // azimuth/pitch/roll
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        this.deviceData?.azimuth  = Math.toDegrees(orientation[0].toDouble()).toInt()
        this.deviceData?.pitch  = Math.toDegrees(orientation[1].toDouble()).toInt()
        this.deviceData?.roll  = Math.toDegrees(orientation[2].toDouble()).toInt()

        //this.deviceData?.roll= ((Math.toDegrees(orientation[2].toDouble()) + 360f) % 360f).toInt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("DeviceOrientation", "Orientation compass unreliable")
        }
    }

    fun resume() {
        mSensorManager?.registerListener(this,
            mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun pause() {
        mSensorManager?.unregisterListener(this)
    }
}