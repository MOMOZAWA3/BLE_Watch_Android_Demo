package com.starmax.sdkdemo.utils.jieli

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.jieli.bluetooth_connect.impl.BluetoothManager
import com.jieli.bluetooth_connect.interfaces.callback.BluetoothEventCallback
import com.jieli.jl_bt_ota.constant.ErrorCode
import com.jieli.jl_bt_ota.constant.StateCode
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager
import com.jieli.jl_bt_ota.interfaces.BtEventCallback
import com.jieli.jl_bt_ota.interfaces.IActionCallback
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback
import com.jieli.jl_bt_ota.model.base.BaseError
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import java.util.UUID

class OtaManager(val otaViewModel: OtaViewModel, var bluetoothManager: BluetoothManager?) : BluetoothOTAManager(otaViewModel.context) {
    var isListening = false

    fun notifyDeviceStatus(device: BluetoothDevice?, status: Int){
        val connectStatus = changeConnectStatus(status)
        onBtDeviceConnection(device,connectStatus)
    }

    fun startListen(){
        isListening = true
    }

    fun stopListen(){
        isListening = false
    }

    override fun getConnectedDevice(): BluetoothDevice? {
        return bluetoothManager?.connectedDevice
    }

    override fun getConnectedBluetoothGatt(): BluetoothGatt? {
        return bluetoothManager?.connectedBluetoothGatt
    }
    /**
     * 连接蓝牙设备
     * <p>
     *  注意:1. 目前的回连方式都是回连BLE设备，只需要实现回连设备的BLE
     *  2. 该方法用于设备回连过程，如果客户是双备份OTA或者自行实现回连流程，不需要实现
     * </p>
     * @param device 通讯方式的蓝牙设备
     */
    override fun connectBluetoothDevice(device: BluetoothDevice?) {
        //回连
        bluetoothManager?.connectBtDevice(device,1)
    }

    override fun disconnectBluetoothDevice(device: BluetoothDevice?) {
        //回连
        bluetoothManager?.disconnectBtDevice(device)
    }

    /**
     *发送数据到蓝牙设备
     *<p>
     *  注意: 1. 需要实现可靠的大数据传输<br/>
     *  1.1 如果是BLE发送数据，需要根据MTU进行分包，然后队列式发数，确保数据发出<br/>
     *  1.2 如果是BLE发送数据 而且 协商MTU大于128， 建议发送MTU = 协商MTU - 6， 进行边缘保护
     *  2. 该方法在发送数据时回调，发送的数据是组装好的RCSP命令。一般长度在[10, 525]
     *</p>
     * @param device 已连接的蓝牙设备
     * @param data 数据包
     * @return 操作结果
     */
    override fun sendDataToDevice(p0: BluetoothDevice?, data: ByteArray?): Boolean {
        bluetoothManager?.sendDataToDevice(p0,data)

        return true
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

    fun tryOta(){
        if(bluetoothManager?.connectedDevice != null){
            registerBluetoothCallback(object : BtEventCallback(){
                override fun onConnection(p0: BluetoothDevice?, status: Int) {
                    Log.d(TAG,"当前状态"+status)

                    if(status == StateCode.CONNECTION_OK){
                        if(isOTA) return; //如果已经在OTA流程，则不需要处理

                        bluetoothOption?.firmwareFilePath = otaViewModel.localPath
                        //1.可以查询是否需要强制升级
                        queryMandatoryUpdate(object : IActionCallback<TargetInfoResponse> {
                            override fun onSuccess(deviceInfo: TargetInfoResponse) {
                                //TODO:说明设备需要强制升级，请跳转到OTA界面，引导用户升级固件
                                deviceInfo.versionCode //设备版本号
                                deviceInfo.versionName //设备版本名
                                deviceInfo.projectCode //设备产品ID(默认是0，如果设备支持会改变)
                                //需要看固件是否支持
                                deviceInfo.uid //客户ID
                                deviceInfo.pid //产品ID
                                //进行步骤2
                            }

                            override fun onError(baseError: BaseError) {
                                //可以不用处理，也可以获取设备信息
                                //没有错误，可以获取设备信息
                                if (baseError.code == ErrorCode.ERR_NONE && baseError.subCode == ErrorCode.ERR_NONE) {
                                    val deviceInfo = deviceInfo
                                    deviceInfo.versionCode //设备版本号
                                    deviceInfo.versionName //设备版本名
                                    deviceInfo.projectCode //设备产品ID(默认是0，如果设备支持会改变)
                                    //需要看固件是否支持
                                    deviceInfo.uid //客户ID
                                    deviceInfo.pid //产品ID
                                    //进行步骤2
                                }
                            }
                        })

                        val that = this

                        startOTA(object : IUpgradeCallback {
                            override fun onStartOTA() {
                                otaViewModel.otaMessage.postValue("JieLi.PROGRESS_STARTED")
                            }

                            override fun onNeedReconnect(addr: String?, isNewReconnectWay: Boolean) {
                                //回调需要回连的设备地址
                                //如果客户设置了BluetoothOTAConfigure#setUseReconnect()为true，则需要在此处回调进行自定义回连设备流程
                                if(bluetoothOption?.isUseReconnect == true) {
                                    //2-1 进行自定义回连流程
                                    bluetoothManager?.connectBLEDevice(
                                        BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr))
                                }
                            }

                            override fun onProgress(type: Int, progress: Float) {
                                otaViewModel.otaMessage.postValue("JieLi.当前进度：${progress}/100")
                            }

                            override fun onStopOTA() {
                                otaViewModel.otaMessage.postValue("JieLi.PROGRESS_STOP_OTA")
                                unregisterBluetoothCallback(that)
                            }

                            override fun onCancelOTA() {
                                otaViewModel.otaMessage.postValue("JieLi.PROGRESS_STOP_OTA")
                            }

                            override fun onError(error: BaseError?) {
                                otaViewModel.otaMessage.postValue("${error?.message}")
                            }
                        })
                    }
                }
            })

            onBtDeviceConnection(bluetoothManager?.connectedDevice,StateCode.CONNECTION_OK)
        }
    }
}