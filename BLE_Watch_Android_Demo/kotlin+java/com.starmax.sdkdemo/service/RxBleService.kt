package com.starmax.sdkdemo.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi


class RxBleService : Service(){
    // Binder given to clients
    private val binder = RCBinder()
    private var handler: Handler? = null
    private var backgroundNotification: BackgroundNotification? = null
    companion object {
        private const val TAG = "RxBleService"
        private val ONGOING_NOTIFICATION_ID = 75430
        private val CHANNEL_ID = "sdkdemo_channel_01"
    }

    interface IOnResultListener{
        fun onWriteSuccess(result: ByteArray);
        fun onWriteError(error: Throwable);
    }

    inner class RCBinder : Binder() {
        val service: RxBleService
            get() = this@RxBleService
    }

    override fun onCreate() {
        super.onCreate()
        backgroundNotification = BackgroundNotification(
            applicationContext,
            CHANNEL_ID,
            ONGOING_NOTIFICATION_ID
        )
        backgroundNotification!!.updateOptions(NotificationOptions(
            channelName = "SdkDemo msg service",
            title = "SdkDemo will get the notification message and send it to the device"
        ), isVisible = true)
    }

    fun onForground(){
        val notification = backgroundNotification!!.build()
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        Log.e(TAG,"开启通知")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onStopForground(){
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.e(TAG,"关闭通知")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Binding to RxBleService.")
        onForground()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Unbinding from RxBleService.")
        onStopForground()
        return super.onUnbind(intent)
    }
}
