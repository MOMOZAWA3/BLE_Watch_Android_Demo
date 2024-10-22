package com.starmax.sdkdemo.viewmodel

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.starmax.bluetoothsdk.Utils

class ScanViewModel(

) : ViewModel() {
    var devices: List<BleDevice> by mutableStateOf(emptyList())
    var deviceNames : HashMap<String,String> = hashMapOf()
    var broadcast: HashMap<String,String> = hashMapOf()
    var searchName: String by mutableStateOf("")
    var searchMac: String by mutableStateOf("")
    var isScanning: Boolean by mutableStateOf(false)

    fun getDeviceName(index:Int) : String {
        val name = devices[index].name;
        if(name != null){
            return name;
        }

        if(deviceNames.containsKey(devices[index].mac)){
            return deviceNames[devices[index].mac]!!
        }
        return "";
    }

    fun startScan() {
        val newDevices : MutableList<BleDevice> = mutableListOf()
        isScanning = true
        BleManager.getInstance().initScanRule(BleScanRuleConfig.Builder()
            .setScanTimeOut(10000)
            .build())
        BleManager.getInstance().scan(object :BleScanCallback(){
            override fun onScanStarted(success: Boolean) {

            }

            override fun onScanning(bleDevice: BleDevice?) {
                if(bleDevice != null && bleDevice.rssi >= -100 && bleDevice.name?.contains(searchName) == true && bleDevice.mac?.contains(searchMac) == true && devices.size <= 100){
                    var isChecked = true
                    var i = 0
                    while (i < bleDevice.scanRecord.size - 1){
                        val len = bleDevice.scanRecord[i].toInt()
                        val type = bleDevice.scanRecord[i + 1].toUByte()
                        val rawData = bleDevice.scanRecord.slice(i + 2 until i + 1 + len).toByteArray()
                        i += 1 + len

                        if(type == 0x01.toUByte() && len >= 1 && rawData.first() == 0x0A.toByte()){
                            broadcast[bleDevice.mac] = "支持一键双连"
                        }

                        if(type == 0xFF.toUByte()){
                            val firstData = rawData.slice(0 .. 1).toByteArray()
                            if(firstData.contentEquals(byteArrayOf(0x00,0x01))){
                                isChecked = true
                            }else if(firstData.contentEquals(byteArrayOf(0x00,0x02))){
                                if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(
                                        0xAA.toByte(), 0xEE.toByte()
                                    ))){

                                    broadcast[bleDevice.mac] =
                                        "SN:"+Utils.bytesToHex(rawData.slice(4 .. 6).toByteArray().reversedArray()) +","+
                                                "心率:"+rawData[7].toInt().toString() + "," +
                                                "步数:"+ Utils.byteArray2Sum(rawData.slice(8..10)).toString() + "," +
                                                "血压:"+ (rawData[11].toInt() and 0xFF).toString() + "/" + (rawData[12].toInt() and 0xFF).toString()  + "," +
                                                "血氧:"+rawData[13].toInt().toString() + "," +
                                                "血糖:"+rawData[14].toInt().toString() + "," +
                                                "温度:"+Utils.byteArray2Sum(rawData.slice(15..16)).toString() + "," +
                                                "梅脱:"+rawData[17].toInt().toString() + "," +
                                                "MAI:"+rawData[18].toInt().toString() + "," +
                                                "压力:"+rawData[19].toInt().toString() + "," +
                                                if(rawData.size > 20){
                                                    "卡路里:" +Utils.byteArray2Sum(rawData.slice(20..22)).toString() + ","+
                                                            "电量:" +rawData[23].toInt().toString()
                                                }else{
                                                    ""
                                                }

                                }
                                if(rawData.slice(2 .. 3).toByteArray().contentEquals(byteArrayOf(
                                        0xBB.toByte(), 0xEE.toByte()
                                    ))){

                                    broadcast[bleDevice.mac] =
                                        "SN:"+Utils.bytesToHex(rawData.slice(4 .. 6).toByteArray().reversedArray()) +","+
                                                "BATB:"+Utils.byteArray2Sum(rawData.slice(7 .. 9).toByteArray())+","+
                                                "电量等级:"+rawData[10].toInt().toString() + "," +
                                                "ADC:"+Utils.byteArray2Sum(rawData.slice(11 .. 13).toByteArray())+","+
                                                "电量:"+rawData[14].toInt().toString()

                                }
                            }
                        }
                    }

                    if(isChecked){
                        deviceNames[bleDevice.mac] = bleDevice.name
                        newDevices.add(bleDevice)
                    }

                    devices = newDevices.toList()
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                isScanning = false
            }

        })
    }

    fun stopScan(){
        if(isScanning){
            BleManager.getInstance().cancelScan()
        }
    }
}