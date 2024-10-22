package com.starmax.sdkdemo.ota

import com.realsil.sdk.dfu.DfuConstants
import com.realsil.sdk.dfu.model.DfuConfig
import com.realsil.sdk.dfu.model.DfuProgressInfo
import com.realsil.sdk.dfu.model.Throughput
import com.realsil.sdk.dfu.utils.ConnectParams
import com.realsil.sdk.dfu.utils.DfuAdapter
import com.realsil.sdk.dfu.utils.GattDfuAdapter
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import java.lang.ref.SoftReference

class Real(val otaViewModel: OtaViewModel) {
    var mDfuAdapter : SoftReference<GattDfuAdapter> = SoftReference(null)

    private val mDfuAdapterCallback = object : DfuAdapter.DfuHelperCallback()
    {
        override fun onStateChanged(state: Int) {
            super.onStateChanged(state)
            println(state)
            if (state == DfuAdapter.STATE_INIT_OK) {
                otaViewModel.otaMessage.postValue("DfuAdapter初始化成功")
            }else if(state == DfuAdapter.STATE_PREPARED){
                val otaDeviceInfo = mDfuAdapter.get()!!.otaDeviceInfo
                otaViewModel.otaMessage.postValue("DfuAdapter升级准备\n"+
                        otaDeviceInfo.toString()
                )

                val mDfuConfig = DfuConfig()
                mDfuConfig.channelType = DfuConfig.CHANNEL_TYPE_GATT
                mDfuConfig.isVersionCheckEnabled = false
                mDfuConfig.address = otaViewModel.mac
                mDfuConfig.filePath = otaViewModel.localPath
                mDfuConfig.fileLocation = DfuConfig.FILE_LOCATION_SDCARD
                val modeInfo = mDfuAdapter.get()!!.getPriorityWorkMode(DfuConstants.OTA_MODE_SILENT_FUNCTION)
                mDfuConfig.otaWorkMode = modeInfo.workmode
                mDfuAdapter.get()!!.startOtaProcedure(mDfuConfig)

            }else if(state == DfuAdapter.STATE_DISCONNECTED){
                otaViewModel.otaMessage.postValue("DfuAdapter断开连接")
            }else if(state == DfuAdapter.STATE_CONNECT_FAILED){
                otaViewModel.otaMessage.postValue("DfuAdapter连接失败")
            }else{
                otaViewModel.otaMessage.postValue("未知状态")
            }
        }

        override fun onProcessStateChanged(state: Int, throughput: Throughput?) {
            super.onProcessStateChanged(state, throughput)
            println(state)
            if(state == DfuConstants.PROGRESS_STARTED){
                otaViewModel.otaMessage.postValue("DfuConstants.PROGRESS_STARTED")
            }else if(state == DfuConstants.PROGRESS_START_DFU_PROCESS){
                otaViewModel.otaMessage.postValue("DfuConstants.PROGRESS_START_DFU_PROCESS")
            }else if(state == DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS){
                otaViewModel.otaMessage.postValue("DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS")
            }else{
                otaViewModel.otaMessage.postValue("DfuConstants${state}")
            }
        }

        override fun onProgressChanged(dfuProgressInfo: DfuProgressInfo?) {
            println(dfuProgressInfo)
            super.onProgressChanged(dfuProgressInfo)
            otaViewModel.otaMessage.postValue("当前进度：${dfuProgressInfo!!.progress}/100")
        }

        override fun onError(type: Int, code: Int) {
            println(code)
            otaViewModel.otaMessage.postValue("发生错误：type.${type},code.${code}")
        }
    }

    init {
        mDfuAdapter = SoftReference(GattDfuAdapter.getInstance(otaViewModel.context))
        mDfuAdapter.get()!!.initialize(mDfuAdapterCallback)
    }

    fun connectRemoteDevice() {
        val connectParamsBuilder = ConnectParams.Builder()
            .address(otaViewModel.mac)
            .reconnectTimes(3)
            .batteryValueFormat(ConnectParams.BATTERY_VALUE_F1)

        mDfuAdapter.get()!!.connectDevice(connectParamsBuilder.build())
    }
}