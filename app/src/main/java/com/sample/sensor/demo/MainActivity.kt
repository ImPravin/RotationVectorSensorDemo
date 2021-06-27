package com.sample.sensor.demo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sample.mylibrary.DeviceData
import com.sample.mylibrary.IRemoteService
import com.sample.mylibrary.IRemoteServiceCallback

class MainActivity : AppCompatActivity() {
    private var mService: IRemoteService? = null
    private var mCallbackText: TextView? = null
    private var mIsBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var button = findViewById<View>(R.id.bind) as Button
        button.setOnClickListener(mBindListener)
        button = findViewById<View>(R.id.unbind) as Button
        button.setOnClickListener(mUnbindListener)

        mCallbackText = findViewById<View>(R.id.callback) as TextView
        mCallbackText?.text = "Not attached."
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder,
        ) {
            mService = IRemoteService.Stub.asInterface(service)
            mCallbackText?.text = "Attached."
            try {
                mService?.registerCallback(mCallback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            Toast.makeText(this@MainActivity, "remote_service_connected",
                Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            mCallbackText?.text = "Disconnected."
            Toast.makeText(this@MainActivity, "remote_service_disconnected",
                Toast.LENGTH_SHORT).show()
        }
    }

    private val mBindListener =
        View.OnClickListener {
            val intent = Intent(this@MainActivity, RotationVectorRemoteService::class.java)
            intent.action = IRemoteService::class.java.name
            bindService(intent, mConnection, BIND_AUTO_CREATE)
            mIsBound = true
            mCallbackText?.text = "Binding Service."
        }

    private val mUnbindListener = View.OnClickListener {
        if (mIsBound) {
            if (mService != null) {
                try {
                    mService?.unregisterCallback(mCallback)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
            mCallbackText?.text = "Unbinded Service."
        }
    }

    private val mCallback: IRemoteServiceCallback = object : IRemoteServiceCallback.Stub() {
        override fun valueChanged(value: DeviceData) {
            runOnUiThread {

                mCallbackText?.text = "Received from service: \n" +
                        getString(R.string.value, value.azimuth, value.pitch, value.roll)
            }
        }
    }
}