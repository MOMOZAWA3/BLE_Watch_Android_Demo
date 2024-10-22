package com.starmax.sdkdemo.viewmodel

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clj.fastble.data.BleDevice
import com.starmax.net.repository.FirmwareRepository
import com.starmax.sdkdemo.ota.BK
import com.starmax.sdkdemo.ota.JieLi
import com.starmax.sdkdemo.ota.Real
import com.starmax.sdkdemo.utils.NetFileUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.*
import java.lang.ref.SoftReference

enum class OtaType{
    Real,
    BK,
    JieLi
}
class OtaViewModel() : ViewModel(),KoinComponent {

    var saveFileName = ""
    var otaMessage = MutableLiveData("")
    var mac : String = ""
    var saveName: String = ""
    var localPath: String = ""
    var bleModel: String = ""
    var bleVersion: String = ""
    var real: Real? = null
    var bk: BK? = null
    var jieLi : JieLi? = null
    var otaType: OtaType? = null

    val context : Context by inject()

    var otaBleDevice: SoftReference<BleDevice>? by mutableStateOf(null)
        private set

    companion object{

    }

    init {
        initDfu()
    }

    fun initDfu(){
        initPath()
        real = Real(this)
        bk = BK(this)
        jieLi = JieLi(this)
    }

    fun initPath(){
        var basepath = context.getExternalFilesDir(null)?.path
        if(basepath == null){
            basepath = Environment.getExternalStorageDirectory().absolutePath
        }
        saveFileName = basepath + "/SDKDemo/Device_update/"
        val file = File(saveFileName)
        if (!file.exists()) file.mkdirs()
    }

    fun download(bleDevice: BleDevice, newBleModel: String, newBleVersion: String){
        otaBleDevice = SoftReference(bleDevice)
        mac = bleDevice.mac
        bleModel = newBleModel
        bleVersion = newBleVersion
        downLoadFile()
    }

    fun getFromPath(bleDevice:BleDevice,uri:Uri){
        otaBleDevice = SoftReference(bleDevice)
        mac = bleDevice.mac
        val file = File(saveFileName)
        if (!file.exists()) file.mkdirs()
        localPath = saveFileName + "ota.bin"
        val fis = context.contentResolver.openInputStream(uri) ?: return
        val apkFile = File(localPath)
        if (apkFile.exists()) apkFile.delete()
        NetFileUtils.copyUpdateFile(fis,File(localPath)) {
            startOta()
        }
    }

    fun connect(bleDevice:BleDevice?){
        otaBleDevice = SoftReference(bleDevice)
        otaBleDevice!!.get()?.let { jieLi?.connectDevice(it.device) }
    }

    fun onStartLog(){
        jieLi?.onStartLog()
    }

    private fun downLoadFile() {
        object : Thread() {
            override fun run() {
                FirmwareRepository.getVersion(model = bleModel,version = bleVersion, onSuccess = { firmware,response ->
                    if(firmware == null) return@getVersion
                    val file = File(saveFileName)
                    if (!file.exists()) file.mkdirs()
                    val url = firmware.binUrl
                    saveName = url.substring( url.lastIndexOf('/')+1, url.length )
                    localPath = saveFileName + saveName
                    println("localPath:"+localPath)
                    val apkFile = File(localPath)
                    if (apkFile.exists()) apkFile.delete()
                    object : Thread() {
                        override fun run() {
                            try {
                                NetFileUtils.downloadUpdateFile(firmware.binUrl,apkFile){
                                    startOta()
                                }
                            } catch (e: java.lang.Exception) {
                                otaMessage.postValue("服务器错误")
                                e.printStackTrace()
                            }
                        }
                    }.start()
                },onError = {
                        e ->
                    otaMessage.postValue("服务器错误")
                    e?.printStackTrace()
                })
            }
        }.start()
    }

    fun startOta(){
        if(otaType == OtaType.BK){
            bk?.startOta()
        }else if(otaType == OtaType.Real){
            real?.connectRemoteDevice()
        } else if(otaType == OtaType.JieLi){
            jieLi?.startOta()
        }
    }
}