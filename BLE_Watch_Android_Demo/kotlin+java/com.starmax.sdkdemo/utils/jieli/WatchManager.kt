package com.starmax.sdkdemo.utils.jieli

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.text.format.DateFormat
import android.util.Log
import com.jieli.bluetooth_connect.impl.BluetoothManager
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback
import com.jieli.jl_rcsp.constant.StateCode
import com.jieli.jl_rcsp.impl.RcspAuth
import com.jieli.jl_rcsp.impl.RcspAuth.OnRcspAuthListener
import com.jieli.jl_rcsp.impl.WatchOpImpl
import com.jieli.jl_rcsp.task.TaskListener
import com.jieli.jl_rcsp.task.logcat.ReadLogcatTask
import com.jieli.jl_rcsp.util.JL_Log
import com.starmax.sdkdemo.utils.NetFileUtils
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID

/**
 * 实现健康管理类
 */
class WatchManager(val otaViewModel: OtaViewModel, var bluetoothManager: BluetoothManager?) : WatchOpImpl(FUNC_WATCH){
    var isListening = false

    fun startListen(){
        isListening = true
    }

    fun stopListen(){
        isListening = false
    }

    fun notifyDeviceStatus(device: BluetoothDevice?, status: Int){
        val connectStatus = changeConnectStatus(status)
        notifyBtDeviceConnection(device,connectStatus)
    }

    //连接状态转换
    private fun changeConnectStatus(status: Int): Int {
        var changeStatus = StateCode.CONNECTION_DISCONNECT
        when (status) {
            BluetoothProfile.STATE_DISCONNECTED, BluetoothProfile.STATE_DISCONNECTING -> {
                changeStatus = StateCode.CONNECTION_DISCONNECT
            }

            BluetoothProfile.STATE_CONNECTED -> changeStatus = StateCode.CONNECTION_OK
            BluetoothProfile.STATE_CONNECTING -> changeStatus = StateCode.CONNECTION_CONNECTING
        }
        return changeStatus
    }
    /**
     * 获取当前连接的设备，sdk的操作都是基于该设备
     * @return 目标设备
     */
    override fun getConnectedDevice(): BluetoothDevice? {
        return bluetoothManager?.connectedDevice
    }

    /**
     * SDK通知外部需要发送数据
     * @param device 蓝牙设备对象
     * @param data   数据包 byte数组
     * @return false：发送失败  true:发送成功
     */
    override fun sendDataToDevice(device: BluetoothDevice?, data: ByteArray): Boolean {
        return bluetoothManager?.sendDataToDevice(device, data) ?: false
    }

    fun startLog(){
        val task = ReadLogcatTask(this)
        //设置监听器
        task.setListener(object : TaskListener {
            override fun onBegin() {
                //回调任务开始
                otaViewModel.otaMessage.postValue("JieLi.ReadLogcatTask Begin")
            }

            override fun onProgress(progress: Int) {
                //回调任务开始
                otaViewModel.otaMessage.postValue("JieLi.ReadLogcatTask Progress"+progress+"/100")
            }

            override fun onFinish() {
                //回调任务完成
                JL_Log.i("JieLiLog", "ReadLogcatTask: onFinish : read logcat size = " + task.result.size)
                val saveName = DateFormat.format("yyyy-MM-dd hh:mm:ss", System.currentTimeMillis()).toString()+otaViewModel.otaBleDevice!!.get()!!.name.toString()+"_log.bin"
                val localPath = otaViewModel.saveFileName + saveName
                val file = File(otaViewModel.saveFileName)
                if (!file.exists()) file.mkdirs()
                val apkFile = File(localPath)
                if (apkFile.exists()) apkFile.delete()
                Log.d("WatcherManager",localPath)
                NetFileUtils.copyUpdateFile(
                    ByteArrayInputStream(task.result),
                    File(localPath)
                ) {
                    otaViewModel.otaMessage.postValue("JieLi.ReadLogcatTask Finish savePath:"+localPath)
                }

            }

            override fun onError(code: Int, msg: String) {
                otaViewModel.otaMessage.postValue("ReadLogcatTask: onError : $code, $msg")
                //回调任务异常信息
                JL_Log.w("JieLiLog", "ReadLogcatTask: onError : $code, $msg")
            }

            override fun onCancel(reason: Int) {
                //回调任务被取消, 该任务暂不支持取消操作
            }
        })

        //执行任务
        task.start()
    }

}