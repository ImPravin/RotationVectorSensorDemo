package com.sample.sensor.demo

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import com.sample.mylibrary.DeviceData
import com.sample.mylibrary.IRemoteService
import com.sample.mylibrary.IRemoteServiceCallback
import com.sample.mylibrary.RotationVectorListener

class RotationVectorRemoteService : Service() {
    private val mCallbacks = RemoteCallbackList<IRemoteServiceCallback>()

    private lateinit var rotationVectorListener: RotationVectorListener

    companion object {
        private const val REPORT_MSG = 1
    }

    override fun onCreate() {
        rotationVectorListener = RotationVectorListener.getInstance(this)
        mHandler.sendEmptyMessage(REPORT_MSG)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("RemoteService", "Received start id $startId: $intent")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        mCallbacks.kill()
        mHandler.removeMessages(REPORT_MSG)
    }

    override fun onBind(intent: Intent): IBinder? {
        return if (IRemoteService::class.java.name == intent.action) {
            mBinder
        } else null
    }

    /**
     * The IRemoteInterface is defined through IDL
     */
    private val mBinder: IRemoteService.Stub = object : IRemoteService.Stub() {
        override fun registerCallback(cb: IRemoteServiceCallback) {
            rotationVectorListener.resume()
            mCallbacks.register(cb)
        }

        override fun unregisterCallback(cb: IRemoteServiceCallback) {
            rotationVectorListener.pause()
            mCallbacks.unregister(cb)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Toast.makeText(this, "Task removed: $rootIntent", Toast.LENGTH_LONG).show()
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            // It is time to bump the value!
            if (msg.what == REPORT_MSG) { // Up it goes.
                val value: DeviceData? = rotationVectorListener.getDeviceData()

                // Broadcast to all clients the new value.
                val N = mCallbacks.beginBroadcast()
                for (i in 0 until N) {
                    try {
                        mCallbacks.getBroadcastItem(i).valueChanged(value)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
                mCallbacks.finishBroadcast()

                // Repeat every 1 second.
                sendMessageDelayed(obtainMessage(REPORT_MSG), 100)
            } else {
                super.handleMessage(msg)
            }
        }
    }
}