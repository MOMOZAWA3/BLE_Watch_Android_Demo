package com.starmax.sdkdemo

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.clj.fastble.BleManager
import com.realsil.sdk.core.RtkConfigure
import com.realsil.sdk.core.RtkCore
import com.realsil.sdk.dfu.RtkDfu
import com.starmax.bluetoothsdk.BleConstant
import com.starmax.net.NetConstant

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(0, 1000)
            .setConnectOverTime(10000)
            .setOperateTimeout(5000)

        val configure = RtkConfigure.Builder()
            .debugEnabled(true)
            .printLog(true)
            .logTag("OTA")
            .build()
        RtkCore.initialize(this,configure)
        RtkDfu.initialize(this,true)

        Log.v(TAG,"蓝牙SDK当前版本号:"+BleConstant.Version)
        Log.v(TAG,"NetSDK当前版本号:"+NetConstant.Version)

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ), 1000
        )

        setContent {
            ComposeNavigation()
        }
    }
}

@Composable
fun ComposeNavigation(){
    val navController = rememberNavController()

    MyNavHost(navController = navController)
}
