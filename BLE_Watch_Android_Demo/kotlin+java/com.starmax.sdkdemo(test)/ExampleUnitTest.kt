package com.starmax.sdkdemo

import android.util.Log
import com.starmax.bluetoothsdk.MapStarmaxNotify
import com.starmax.bluetoothsdk.StarmaxSend
import com.starmax.bluetoothsdk.StarmaxSendRequest
import com.starmax.bluetoothsdk.Utils
import com.starmax.bluetoothsdk.data.CallControlType
import com.starmax.bluetoothsdk.data.Contact
import com.starmax.bluetoothsdk.data.EventReminder
import com.starmax.net.data.Dial
import com.starmax.net.data.Firmware
import com.starmax.net.repository.DialRepository
import com.starmax.net.repository.FirmwareRepository
import com.starmax.net.response.AbstractResponse
import org.junit.Test

import org.junit.Assert.*
import java.util.Calendar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test1K(){
    }

    @Test
    fun test() {
        print( (-6 + 24) % 24)
        print((6+24) % 24)
    }

    @Test
    fun test1() {
        val lineByteArray = byteArrayOf(0x80.toByte(),0x01,0x80.toByte(),0x03)
        print(lineByteArray.asList().chunked(2).map { minuteByteArray -> minuteByteArray.reversed().map {  minuteByte -> String.format("%02X", minuteByte) }.joinToString("")}.joinToString(","))
    }

    @Test
    fun test2(){
        val bytes ="Колыбельная".toByteArray()
        val stringBuilder = StringBuilder()
        val lineLength = 20 // 每行的字节数

        for (i in bytes.indices) {
            stringBuilder.append(String.format("%02X ", bytes[i])) // 将字节转换为十六进制字符串
            if ((i + 1) % lineLength == 0) {
                stringBuilder.append("\n") // 在每行结尾插入换行符
            }
        }
        print(stringBuilder.toString())
    }
}