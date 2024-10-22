package com.starmax.sdkdemo.ota

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.jieli.bluetooth_connect.bean.BluetoothOption
import com.jieli.bluetooth_connect.constant.BluetoothConstant
import com.jieli.bluetooth_connect.impl.BluetoothManager
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure
import com.jieli.jl_rcsp.impl.RcspAuth
import com.starmax.sdkdemo.utils.jieli.OtaManager
import com.starmax.sdkdemo.utils.jieli.WatchManager
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import java.util.UUID


class JieLi(val otaViewModel: OtaViewModel) {
    var otaManager : OtaManager? = null
    var watchManager: WatchManager? = null
    var bluetoothManager : BluetoothManager? = null
    var rcspAuth: RcspAuth? = null
    var isAuthed = false
    var mRcspAuthListener: RcspAuth.OnRcspAuthListener =
        object : RcspAuth.OnRcspAuthListener {
            override fun onInitResult(b: Boolean) {}
            override fun onAuthSuccess(bluetoothDevice: BluetoothDevice) {
                Log.e("mRcspAuthListener", "-onAuthSuccess- device : ")
                isAuthed = true
                if(watchManager?.isListening == true){
                    watchManager?.notifyDeviceStatus(bluetoothDevice,BluetoothProfile.STATE_CONNECTED)
                }else{
                    otaManager?.notifyDeviceStatus(bluetoothDevice,BluetoothProfile.STATE_CONNECTED)
                }
            }

            override fun onAuthFailed(bluetoothDevice: BluetoothDevice, i: Int, s: String) {
                Log.e("mRcspAuthListener", "-onAuthFailed- device : ")
                isAuthed = false
                bluetoothManager?.disconnectBLEDevice(bluetoothDevice)
            }
        }

    init {
        val bluetoothOption = BluetoothOption.createDefaultOption()
            .setPriority(BluetoothConstant.PROTOCOL_TYPE_SPP)
            .setScanFilterData("")
            .setMtu(BluetoothConstant.BLE_MTU_MAX)
            .setNeedChangeBleMtu(true)
            .setBleScanStrategy(BluetoothConstant.NONE_FILTER)
            .setUseMultiDevice(true)
            .setUseDeviceAuth(true)
        bluetoothManager = BluetoothManager(otaViewModel.context,bluetoothOption)
        initRcsp()
        initOta()
        initLog()
        initCallback()
    }

    fun initRcsp(){
        isAuthed = false
        rcspAuth = RcspAuth({
                device,data ->
            bluetoothManager?.sendDataToDevice(device, data)?: false
        },mRcspAuthListener)
    }

    fun initOta(){
        val bluetoothOtaOption = BluetoothOTAConfigure.createDefault()
        bluetoothOtaOption.setPriority(BluetoothOTAConfigure.PREFER_SPP) //请按照项目需要选择
            .setUseAuthDevice(false) //具体根据固件的配置选择
            .setBleIntervalMs(500) //默认是500毫秒
            .setTimeoutMs(3000) //超时时间
            .setMtu(500) //BLE底层通讯MTU值，会影响BLE传输数据的速率。建议用500 或者 270。该MTU值会使OTA库在BLE连接时改变MTU，所以用户SDK需要对此处理。
            .setNeedChangeMtu(true)
        //是否自定义回连方式，默认为false，走SDK默认回连方式，客户可以根据需求进行变更
        otaManager = OtaManager(otaViewModel,bluetoothManager)
        otaManager!!.configure(bluetoothOtaOption) //设置OTA参数
    }

    fun initLog() {
        watchManager = WatchManager(otaViewModel,bluetoothManager)
        watchManager?.startListen()
    }

    fun initCallback(){
        bluetoothManager?.registerBluetoothCallback(object : BluetoothEventCallback() {
            override fun onBleConnection(device: BluetoothDevice?, status: Int) {
                super.onBleConnection(device, status)

                if(status == BluetoothProfile.STATE_CONNECTED){
                    rcspAuth?.stopAuth(device,false)
                    rcspAuth?.startAuth(device)
                }else{
                    isAuthed = false
                    if(watchManager?.isListening == true){
                        watchManager?.notifyDeviceStatus(device,status)
                    }else if(otaManager?.isListening == true){
                        otaManager?.notifyDeviceStatus(device,status)
                    }
                }
            }

            override fun onSppStatus(device: BluetoothDevice?, status: Int) {
                super.onSppStatus(device, status)

                if(status == BluetoothProfile.STATE_CONNECTED){
                    rcspAuth?.stopAuth(device,false)
                    rcspAuth?.startAuth(device)
                }else{
                    isAuthed = false
                    if(watchManager?.isListening == true){
                        watchManager?.notifyDeviceStatus(device,status)
                    }else if(otaManager?.isListening == true){
                        otaManager?.notifyDeviceStatus(device,status)
                    }
                }
            }

            override fun onBleDataNotification(
                device: BluetoothDevice?,
                serviceUuid: UUID?,
                characteristicsUuid: UUID?,
                data: ByteArray?
            ) {
                super.onBleDataNotification(device, serviceUuid, characteristicsUuid, data)
                if(!isAuthed){
                    rcspAuth?.handleAuthData(device,data)
                }else if(watchManager?.isListening == true){
                    watchManager?.notifyReceiveDeviceData(device,data)
                }else if(otaManager?.isListening == true){
                    otaManager?.onReceiveDeviceData(device,data)
                }
            }

            override fun onSppDataNotification(
                device: BluetoothDevice?,
                sppUUID: UUID?,
                data: ByteArray?
            ) {
                super.onSppDataNotification(device, sppUUID, data)
                if(!isAuthed){
                    rcspAuth?.handleAuthData(device,data)
                }else if(watchManager?.isListening == true){
                    watchManager?.notifyReceiveDeviceData(device,data)
                }else if(otaManager?.isListening == true){
                    otaManager?.onReceiveDeviceData(device,data)
                }
            }

            override fun onBleDataBlockChanged(device: BluetoothDevice?, block: Int, status: Int) {
                super.onBleDataBlockChanged(device, block, status)
                if(otaManager?.isListening == true){
                    otaManager?.onMtuChanged(otaManager?.connectedBluetoothGatt,block,status)
                }
            }
        })
    }

    fun startOta(){
        stopListen()
        otaManager?.startListen()
        otaManager?.tryOta()
    }

    fun onStartLog(){
        stopListen()
        watchManager?.startListen()
        watchManager?.startLog()
    }

    fun stopListen(){
        otaManager?.stopListen()
        watchManager?.stopListen()
    }

    fun connectDevice(bluetoothDevice: BluetoothDevice){
        stopListen()
        watchManager?.startListen()
        bluetoothManager?.connectSPPDevice(bluetoothDevice)
    }

    companion object {
        private val TAG: String = "JieLi"
    }
}

