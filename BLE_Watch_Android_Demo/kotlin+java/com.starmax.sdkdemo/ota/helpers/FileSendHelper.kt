package com.starmax.sdkdemo.ota.helpers

import android.graphics.BitmapFactory
import android.os.Environment
import com.starmax.bluetoothsdk.BleFileSenderListener
import com.starmax.bluetoothsdk.BmpUtils
import com.starmax.bluetoothsdk.FileUtils
import java.io.InputStream

class FileSendHelper {
    var allFileData: ByteArray = byteArrayOf()
    var dataSize: Int = 0
    var dataIndex: Int = 0
    var fileSenderListener: BleFileSenderListener? = null
    var bufferSize = 2000
    var sliceBuffer = 0
    var failNum = 0

    fun getSaveFileName(): String{
        return Environment.getExternalStorageDirectory().absolutePath + "/StarmaxDemo/images/"
    }

    fun initFile(inputStream: InputStream?, bleFileSenderListener: BleFileSenderListener) {
        var bytes = FileUtils.inputStream2Bytes(inputStream)
        bytes = bytes ?: byteArrayOf()

        println("bin文件大小:${bytes.size}")

        initData(bytes, bleFileSenderListener)
    }

    fun initFileWithBackground(binInputStream: InputStream?, width:Int, height:Int, imageInputStream: InputStream?, bleFileSenderListener: BleFileSenderListener) {
        var bytes = FileUtils.inputStream2Bytes(binInputStream)
        bytes = bytes ?: byteArrayOf()

        val srcBitmap = BitmapFactory.decodeStream(imageInputStream)
        val bmpBytes = BmpUtils.convert(srcBitmap,width,height)
        val bmpCopy = ByteArray(bmpBytes.size - 54)
        for(i in 54 until bmpCopy.size step 2){
            bmpCopy.set(i - 54,bmpBytes[i+1])
            bmpCopy.set(i+1 - 54,bmpBytes[i])
        }

        //Utils.p(bmpBytes)
        println("bin文件大小:${bytes.size}")
        println("bmp文件大小:${bmpBytes.size}")
        println("bmp数据大小:${bmpCopy.size}")
        println("数据总大小:${bmpCopy.size+bytes.size}")
        initData(bytes+bmpCopy, bleFileSenderListener)
    }

    private fun initData(bytes: ByteArray, bleFileSenderListener: BleFileSenderListener) {
        allFileData = bytes
        println("totalBytes:${bytes.size}")
        fileSenderListener = bleFileSenderListener

        val totalBuffer = bufferSize - sliceBuffer

        dataSize = if (allFileData.size % totalBuffer == 0) {
            (allFileData.size / totalBuffer)
        } else {
            (allFileData.size / totalBuffer) + 1
        }

        dataIndex = 0
    }

    fun onStart() {
        fileSenderListener?.onStart()
    }

    fun onSuccess() {
        fileSenderListener?.onSuccess()
    }

    fun onProgress() {
        if(dataSize == 0){
            fileSenderListener?.onProgress(100.0)
        }else{
            fileSenderListener?.onProgress((dataIndex.toDouble() / dataSize.toDouble()) * 100)
            if(hasNext()){
                fileSenderListener?.onSend()
            }
        }
    }

    fun onFailure() {
        if(failNum < 3){
            dataIndex -= 1
            onProgress()
            failNum += 1
        }else{
            fileSenderListener?.onFailure(4)
        }
    }

    fun sendFile(): ByteArray {
        val totalBuffer = bufferSize - sliceBuffer

        var len = totalBuffer
        if (allFileData.size < totalBuffer * (dataIndex + 1)) {
            len = allFileData.size - (totalBuffer * dataIndex)
        }

        val startIndex = totalBuffer * dataIndex
        val endIndex = startIndex + len - 1

        dataIndex += 1

        val data = allFileData.sliceArray(startIndex..endIndex)
        return data
    }

    fun hasNext(): Boolean {
        val len = bufferSize - sliceBuffer
        return (len * dataIndex) < allFileData.size
    }

}