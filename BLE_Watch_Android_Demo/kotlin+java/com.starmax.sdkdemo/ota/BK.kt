package com.starmax.sdkdemo.ota

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.exception.BleException
import com.starmax.bluetoothsdk.*
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import java.io.FileInputStream
import java.util.*

class BK(val otaViewModel: OtaViewModel) {
//    private var sender: FileSendHelper = FileSendHelper()

//    fun startOta(){
//        val inputStream = FileInputStream(otaViewModel.localPath)
//        sender.sliceBuffer = 0
//        sender.bufferSize = OAD_BLOCK_SIZE
//        sender.initFile(inputStream,object : BleFileSenderListener(){
//            override fun onFailure() {
//                otaViewModel.otaMessage.postValue("发生错误")
//            }
//
//            override fun onProgress(progress: Double) {
//                otaViewModel.otaMessage.postValue("当前进度：${progress}/100")
//            }
//
//            override fun onSend() {
//                if (sender.hasNext()) {
//                    val mOadBuffer = ByteArray(OAD_BUFFER_SIZE)
//                    mOadBuffer[0] = Conversion.loUint16(sender.dataIndex.toLong())
//                    mOadBuffer[1] = Conversion.hiUint16(sender.dataIndex.toLong())
//
//                    System.arraycopy(
//                        sender.sendFile(),
//                        0,
//                        mOadBuffer,
//                        2,
//                        OAD_BLOCK_SIZE
//                    )
//
//                    Utils.p(mOadBuffer)
//
//                    writeOTABlock(mOadBuffer)
//                }
//            }
//
//            override fun onStart() {
//                val buf = ByteArray(16)
//                System.arraycopy(sender.allFileData, 0, buf, 0, 16)
//                writeOTAIdentfy(buf)
//            }
//
//            override fun onSuccess() {
//                otaViewModel.otaMessage.postValue("升级成功")
//            }
//
//        })
//
//
//        sender.onStart()
//
//    }

    fun startOta(){
        val inputStream = FileInputStream(otaViewModel.localPath)
        BleFileSender.initFile(inputStream,
            object :
                BleFileSenderListener() {
                override fun onSuccess() {}

                override fun onProgress(progress: Double) {
                    otaViewModel.otaMessage.postValue("当前进度：${progress}/100")
                }

                override fun onCheckSum() {
                }

                override fun onFailure(status: Int) {}

                override fun onStart() {
                    val data = StarmaxSend()
                        .sendFirmware()
                    sendMsg(data)
                }

                override fun onSend() {
                    if (BleFileSender.hasNext()) {
                        val data = StarmaxSend().sendFile()
                        sendMsg(data)
                    }
                }

                override fun onSendComplete() {
                }
            })

        BleFileSender.sliceBuffer = 8

        BleFileSender.onStart()

    }

    fun writeOTAIdentfy(byteArray: ByteArray?) {
//        BleManager.getInstance().write(
//            otaViewModel.otaBleDevice?.get(),
//            UUID_OTA_SERVICE.toString(),
//            UUID_IDENTFY.toString(),
//            byteArray,
//            object : BleWriteCallback() {
//                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
//                    //bleMessage.value = "指令发送成功"
//                    println("当前 $current 总共 $total")
//                    if(current == total){
//                        sender.onProgress()
//                    }
//                }
//
//                override fun onWriteFailure(exception: BleException?) {
//                    sender.onFailure()
//                }
//            })
    }

    fun sendMsg(data: ByteArray?) {
        BleManager.getInstance().write(
            otaViewModel.otaBleDevice?.get(),
            WriteServiceUUID.toString(),
            WriteCharacteristicUUID.toString(),
            data,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    //bleMessage.value = "指令发送成功"
                    println("当前 $current 总共 $total")
                }

                override fun onWriteFailure(exception: BleException?) {
                    print(exception.toString())
                    //bleMessage.value = "指令发送失败"
                }
            })
    }


    fun writeOTABlock(byteArray: ByteArray?) {
//        BleManager.getInstance().write(
//            otaViewModel.otaBleDevice?.get(),
//            UUID_OTA_SERVICE.toString(),
//            UUID_BLOCK.toString(),
//            byteArray,
//            object : BleWriteCallback() {
//                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
//                    if(current == total){
//                        sender.onProgress()
//                    }
//                }
//
//                override fun onWriteFailure(exception: BleException?) {
//                    sender.onFailure()
//                }
//            })
    }

    companion object {
        private val TAG: String = "BK"
        val FILE_BUFFER_SIZE = 0x40000
        val OAD_BLOCK_SIZE = 16
        val HAL_FLASH_WORD_SIZE = 4
        val OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE

        val UUID_OTA_SERVICE = UUID.fromString("f000ffc0-0451-4000-b000-000000000000")
        val UUID_IDENTFY = UUID.fromString("f000ffc1-0451-4000-b000-000000000000")
        val UUID_BLOCK = UUID.fromString("f000ffc2-0451-4000-b000-000000000000")

        /**
         * 写
         */
        val WriteServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
        val WriteCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9d")
    }
}