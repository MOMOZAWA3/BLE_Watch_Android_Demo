package com.starmax.sdkdemo.utils

import com.starmax.net.NetConstant
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


object TestRepository {
    fun test(text: String){
        println("准备提交"+text)
        Thread(object:Runnable{
            override fun run() {
                sendPost("${NetConstant.BaseApi}/check_connect",text,"")
            }
        }).start();
    }

    fun testLocal(localPath : String,text : String,filename: String){
        try {
            var newFileIndex = 1
            var newFileName = "${filename.replace(".txt","")}.$newFileIndex.txt"
            var newFile = File(localPath, newFileName)

            // 查找合适的文件名，以免覆盖已有文件
            while (newFile.exists() && newFile.length() >= 1 * 1024 * 1024) {
                newFileIndex++
                newFileName = "${filename.replace(".txt","")}.$newFileIndex.txt"
                newFile = File(localPath, newFileName)
            }

            val fos = FileOutputStream(newFile,true)
            fos.write(text.toByteArray())
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendPost(urlStr: String?, dataStr: String, paramsStr: String?): String? {
        var result: String? = ""
        try {
            val data = dataStr.toByteArray(charset("UTF-8"))
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.doInput = true
            conn.useCaches = false
            conn.requestMethod = "POST"
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Charset", "UTF-8")
            conn.setRequestProperty("Content-Length", data.size.toString())
            conn.setRequestProperty("Content-Type", "text/xml")
            conn.connect()
            val out: OutputStream = conn.outputStream
            out.write(data)
            out.flush()
            out.close()
            println(conn.responseCode)
            if (conn.responseCode == 200) {
                println("连接成功")
                val `in`: InputStream = conn.inputStream
                val data1: ByteArray = readInputStream(`in`)!!
                result = String(data1)
            } else {
                println("连接失败")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    @Throws(java.lang.Exception::class)
    fun readInputStream(inStream: InputStream): ByteArray? {
        val outStream = ByteArrayOutputStream()
        val buffer = ByteArray(10240)
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        var len = 0
        while (inStream.read(buffer).also { len = it } != -1) {
            outStream.write(buffer, 0, len)
        }
        inStream.close()
        return outStream.toByteArray()
    }
}
