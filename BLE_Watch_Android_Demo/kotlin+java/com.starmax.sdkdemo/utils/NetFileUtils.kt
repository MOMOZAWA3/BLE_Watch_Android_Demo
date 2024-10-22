package com.starmax.sdkdemo.utils

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object NetFileUtils {
    @Throws(IOException::class)
    fun downloadUpdateFile(fileUrl: String,saveFile: File,onDownloaded : () -> Unit) {
        var updateTotalSize = 0
        var httpConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null
        var downloadSuccess = false
        try {
            val url = URL(fileUrl)
            httpConnection = url.openConnection() as HttpURLConnection
            httpConnection.connectTimeout = 10000
            httpConnection.readTimeout = 20000
            updateTotalSize = httpConnection.contentLength
            println("文件大小"+updateTotalSize)

            inputStream = httpConnection.inputStream

            val bos = ByteArrayOutputStream()
            val buffer = ByteArray(2048)
            var readSize = 0
            while (inputStream.read(buffer).also { readSize = it } > 0) {
                println("正在下载"+readSize)
                bos.write(buffer, 0, readSize)
            }

            bos.close()

            fos = FileOutputStream(saveFile)
            fos.write(bos.toByteArray())
            downloadSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            httpConnection?.disconnect()
            inputStream?.close()
            fos?.close()

            if(downloadSuccess){
                onDownloaded()
            }
        }
    }

    @Throws(IOException::class)
    fun copyUpdateFile(inputStream: InputStream, saveFile: File, onDownloaded: () -> Unit) {
        var fos: FileOutputStream? = null
        var downloadSuccess = false
        try {
            val bos = ByteArrayOutputStream()
            val buffer = ByteArray(2048)
            var readSize = 0
            while (inputStream.read(buffer).also { readSize = it } > 0) {
                Log.e("NetFileUtils","正在复制："+readSize)
                bos.write(buffer, 0, readSize)
            }

            bos.close()

            fos = FileOutputStream(saveFile)
            fos.write(bos.toByteArray())
            downloadSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
            fos?.close()

            if(downloadSuccess){
                onDownloaded()
            }
        }
    }
}