package com.starmax.sdkdemo.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.starmax.bluetoothsdk.BleFileSender
import com.starmax.bluetoothsdk.BleFileSenderListener
import com.starmax.bluetoothsdk.BmpUtils
import com.starmax.bluetoothsdk.Notify
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.StarmaxSend
import com.starmax.bluetoothsdk.StarmaxSendRequest
import com.starmax.bluetoothsdk.Utils
import com.starmax.bluetoothsdk.data.Clock
import com.starmax.bluetoothsdk.data.EventReminder
import com.starmax.bluetoothsdk.data.HistoryType
import com.starmax.bluetoothsdk.data.MessageType
import com.starmax.bluetoothsdk.data.NotifyType
import com.starmax.bluetoothsdk.data.SummerWorldClock
import com.starmax.bluetoothsdk.data.WeatherDay
import com.starmax.bluetoothsdk.factory.SportHistoryFactory
import com.starmax.bluetoothsdk.factory.SummerWorldClockFactory
import com.starmax.bluetoothsdk.factory.WeatherSevenFactory
import com.starmax.net.repository.CrackRepository
import com.starmax.net.repository.UiRepository
import com.starmax.sdkdemo.service.RxBleService
import com.starmax.sdkdemo.utils.NetFileUtils
import com.starmax.sdkdemo.utils.SlmM1Crack
import com.starmax.sdkdemo.utils.TestRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.Integer.min
import java.lang.ref.SoftReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.UUID
import com.starmax.sdkdemo.api.WordpressApiService


enum class BleState {
    DISCONNECTED,
    CONNECTTING,
    CONNECTED
}

class BleViewModel() : ViewModel(), KoinComponent {
    private var savePath = ""
    private var localBasePath = ""

    var tryOpenNotify = mutableStateOf(true)
        private set

    /**
     * 写
     */
    val WriteServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
    val WriteCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9d")

    /**
     * 读
     */
    val NotifyServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d")
    val NotifyCharacteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9d")

    var bleDevice: SoftReference<BleDevice>? by mutableStateOf(null)
        private set

    var bleGatt: SoftReference<BluetoothGatt>? by mutableStateOf(null)
        private set

    var bleModel = ""
    var bleVersion = ""
    var bleUiVersion = ""
    var uiSupportDifferentialUpgrade = false

    var disconnectSubject = PublishSubject.create<Int>()

    var originData = mutableStateOf("")
        private set

    var bleState by mutableStateOf(BleState.DISCONNECTED)
        private set

    var bleStateLiveData = MutableLiveData(BleState.DISCONNECTED)

    var bleMessage = mutableStateOf("")
        private set

    val bleStateLabel: String
        get() {
            val data = when (bleState) {
                BleState.DISCONNECTED -> "已断开"
                BleState.CONNECTTING -> "连接中"
                BleState.CONNECTED -> "已连接"
            }
            return data
        }

    var bleResponse = mutableStateOf("")
        private set
    var bleResponseLabel = mutableStateOf("")
        private set

    val context: Context by inject()

    private val sendDisposable = CompositeDisposable()
    private val messageDisposable = CompositeDisposable()

    var imageUri: Uri? = null
    var binUri: Uri? = null

    var msgType = 0
    var msgContent = 0

    var packageId = 0

    private var bleService: RxBleService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: RxBleService.RCBinder = service as RxBleService.RCBinder
            bleService = binder.service
            Log.e("BleViewModel", "-- RxBleService-- 已连接")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bleService = null
            Log.e("BleViewModel", "-- RxBleService-- 已断连")
        }
    }

    var bleGattCallback: BleGattCallback = object : BleGattCallback() {
        override fun onStartConnect() {
            bleState = BleState.CONNECTTING
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙正在连接"

            // 发送蓝牙状态到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Bluetooth Status",
                content = bleMessage.value,
                status = "publish"
            )
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            bleState = BleState.DISCONNECTED
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙连接失败: ${exception.toString()}"

            // 发送蓝牙状态到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Bluetooth Status",
                content = bleMessage.value,
                status = "publish"
            )
        }

        override fun onConnectSuccess(
            newBleDevice: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            bleDevice = SoftReference(newBleDevice)
            bleState = BleState.CONNECTED
            bleGatt = SoftReference(gatt)
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙连接成功"

            // 发送蓝牙状态到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Bluetooth Status",
                content = bleMessage.value,
                status = "publish"
            )

            Log.d("BleViewModel", gatt?.getService(NotifyServiceUUID).toString())

            if (gatt?.getService(NotifyServiceUUID) == null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    gatt?.discoverServices()
                }, 1000)
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (tryOpenNotify.value) {
                    openNotify(bleDevice!!.get())
                } else {
                    openIndicate(bleDevice!!.get())
                }
            }, 3000)
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            bleState = BleState.DISCONNECTED
            bleGatt = SoftReference(gatt)
            bleStateLiveData.postValue(bleState)
            bleMessage.value = "蓝牙连接断开"

            // 发送蓝牙状态到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Bluetooth Status",
                content = bleMessage.value,
                status = "publish"
            )

            disconnectSubject.onNext(1)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            // 可以在这里根据需要添加逻辑
        }
    }

    init {
        initPath()
        //蓝牙打开、关闭广播监听
        context.registerReceiver(BluetoothListenerReceiver(this), makeFilter())
        StarmaxBleClient.instance.setWrite { byteArray -> sendMsg(byteArray) }
    }

    fun setNotify(boolean: Boolean){
        tryOpenNotify.value = boolean
    }

    fun getDeviceName(): String {
        val name = bleDevice?.get()?.name;
        if (name != null) {
            return name;
        }

        return "";
    }

    fun initPath() {
        var basepath = context.getExternalFilesDir(null)?.path
        if (basepath == null) {
            basepath = Environment.getExternalStorageDirectory().absolutePath
        }
        localBasePath = basepath!!
        savePath = basepath + "/SDKDemo/Device_update/"
        println("下载地址：" + savePath)
    }

    fun connect(newBleDevice: BleDevice?) {
        bleDevice = SoftReference(newBleDevice)
        if (bleDevice != null) {
            BleManager.getInstance().connect(bleDevice!!.get(), bleGattCallback)
        }
    }

    fun getRssi() {
        BleManager.getInstance().readRssi(bleDevice!!.get(), object : BleRssiCallback() {
            override fun onRssiSuccess(rssi: Int) {
                bleResponseLabel.value = "信号强度：" + rssi

                // 发送 RSSI 信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Bluetooth RSSI",
                    content = bleResponseLabel.value,
                    status = "publish"
                )
            }

            override fun onRssiFailure(exception: BleException?) {
                val errorMessage = exception?.description ?: "未知错误"
                bleResponseLabel.value = "读取信号强度失败: $errorMessage"

                // 发送 RSSI 读取失败信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Bluetooth RSSI Failure",
                    content = bleResponseLabel.value,
                    status = "publish"
                )
            }
        })
    }

    fun openIndicate(newBleDevice: BleDevice?) {
        BleManager.getInstance().indicate(
            newBleDevice,
            NotifyServiceUUID.toString(),
            NotifyCharacteristicUUID.toString(),
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    bleMessage.value = "打开indicate成功"
                    handleOpenSuccess()

                    // 发送 Indicate 成功信息到 WordPress
                    val wordpressService = WordpressApiService()
                    wordpressService.sendBluetoothDataToWordpress(
                        title = "Indicate Success",
                        content = bleMessage.value,
                        status = "publish"
                    )
                }

                override fun onIndicateFailure(exception: BleException?) {
                    val errorMessage = exception?.description ?: "未知错误"
                    bleMessage.value = "打开indicate失败：$errorMessage"

                    // 发送 Indicate 失败信息到 WordPress
                    val wordpressService = WordpressApiService()
                    wordpressService.sendBluetoothDataToWordpress(
                        title = "Indicate Failure",
                        content = bleMessage.value,
                        status = "publish"
                    )
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    StarmaxBleClient.instance.notify(data)
                }
            })
    }


    fun openNotify(newBleDevice: BleDevice?) {
        BleManager.getInstance().notify(
            newBleDevice,
            NotifyServiceUUID.toString(),
            NotifyCharacteristicUUID.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    bleMessage.value = "打开notify成功"
                    handleOpenSuccess()

                    // 发送 Notify 成功信息到 WordPress
                    val wordpressService = WordpressApiService()
                    wordpressService.sendBluetoothDataToWordpress(
                        title = "Notify Success",
                        content = bleMessage.value,
                        status = "publish"
                    )
                }

                override fun onNotifyFailure(exception: BleException) {
                    val errorMessage = exception.description ?: "未知错误"
                    bleMessage.value = "打开notify失败：$errorMessage"

                    // 发送 Notify 失败信息到 WordPress
                    val wordpressService = WordpressApiService()
                    wordpressService.sendBluetoothDataToWordpress(
                        title = "Notify Failure",
                        content = bleMessage.value,
                        status = "publish"
                    )
                }

                @SuppressLint("MissingPermission", "NewApi")
                override fun onCharacteristicChanged(data: ByteArray) {
                    Utils.p(data)
                    StarmaxBleClient.instance.notify(data)
                }
            })
    }

    private fun handleOpenSuccess(){
        TestRepository.testLocal(localBasePath, Date().toString() + "\n", "log.txt")
        changeMtu {
            StarmaxBleClient.instance.notifyStream()
                .takeUntil(disconnectSubject)
                .subscribe(
                    {
                        if(it.data is Notify.StepHistory){
                            originData.value =
                                it.byteArray.asList().drop(7).chunked(12).map {
                                        byteArray -> byteArray.map { byte-> String.format("%02X", byte)}.toString()
                                }.joinToString("\n")
                        }else if(it.data is Notify.OriginSleepHistory){
                            originData.value =  it.byteArray.asList().drop(7).chunked(120).mapIndexed {
                                    hourIndex,hourByteArray -> "${hourIndex}小時：\n"+(hourByteArray.chunked(10).map {
                                    lineByteArray -> lineByteArray.chunked(2).map { minuteByteArray -> minuteByteArray.reversed().map {  minuteByte -> String.format("%02X", minuteByte) }.joinToString("")}.joinToString(",")
                            }.joinToString("\n"))
                            }.joinToString("\n")
                        }else{
                            originData.value =
                                it.byteArray.map { String.format("%02X", it) }.toString()
                        }
                        //bleResponse.value = it.data.toString()

                        if (it.data is Notify.Reply) {
                            if((it.data as Notify.Reply).type == NotifyType.Log.name){
                                Utils.p(it.byteArray)
                                TestRepository.testLocal(
                                    localBasePath,
                                    it.byteArray.toString(Charsets.US_ASCII).replace("TAG=","\nTAG="),
                                    "saiwei.txt"
                                )
                            }
                        }

                        if(it.data !is Notify.Diff){
                            if(it.data is Notify.TempHistory){
                                if(!(it.data as Notify.TempHistory).hasNext){
                                    TestRepository.testLocal(localBasePath,
                                        it.byteArray.drop(7).map {
                                            String.format(
                                                "0x%02X",
                                                it
                                            )
                                        }.chunked(40).map {
                                            it.joinToString(",")
                                        }.joinToString(",\n") + ",\n\n\n",
                                        "temp.txt"
                                    )
                                }
                            }else if(it.data is Notify.OriginSleepHistory){
                                if(!(it.data as Notify.OriginSleepHistory).hasNext){
                                    TestRepository.testLocal(localBasePath,
                                        it.byteArray.drop(7).map {
                                            String.format(
                                                "0x%02X",
                                                it
                                            )
                                        }.chunked(40).map {
                                            it.joinToString(",")
                                        }.joinToString(",\n") + ",\n\n\n",
                                        "origin_sleep.txt"
                                    )
                                }
                            }else{
//                                TestRepository.testLocal(localBasePath,
//                                    Date().toString() + "\n" + "\n" + it.byteArray.map {
//                                        String.format(
//                                            "0x%02X",
//                                            it
//                                        )
//                                    }.chunked(40).map {
//                                        it.joinToString(",")
//                                    }.joinToString(",\n") + "\n\n\n",
//                                    "demo-test.txt"
//                                )
                            }

                        }


                    },
                    {

                    }
                ).let {}
            StarmaxBleClient.instance.realTimeDataStream().takeUntil(disconnectSubject)
                .subscribe({
                    // 创建 JSON 数据
                    val realTimeData = JSONObject(mapOf(
                        "gsensor_list" to it.gensorsList.map {
                            hashMapOf(
                                "x" to it.x,
                                "y" to it.y,
                                "z" to it.z,
                            )
                        }.toMutableList(),
                        "steps" to it.steps,
                        "calore" to it.calore,
                        "distance" to it.distance,
                        "heart_rate" to it.heartRate,
                        "blood_pressure_ss" to it.bloodPressureSs,
                        "blood_pressure_fz" to it.bloodPressureFz,
                        "blood_oxygen" to it.bloodOxygen,
                        "temp" to it.temp,
                        "blood_sugar" to it.bloodSugar
                    )).toString()

                    // 更新 UI
                    bleResponse.value = realTimeData

                    // 将实时数据发送到 WordPress
                    val wordpressService = WordpressApiService()
                    wordpressService.sendBluetoothDataToWordpress(
                        title = "Real-time Bluetooth Data",
                        content = realTimeData,
                        status = "publish"
                    )
                }, {
                    Log.e("BleViewModel", "Error receiving real-time data: ${it.message}")
                }).let {}

            StarmaxBleClient.instance.healthMeasureStream()
                .takeUntil(disconnectSubject)
                .subscribe({
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val formatted = current.format(formatter)

                    if (it.status == 0) {
                        var healthData = ""
                        when (it.type) {
                            0x63 -> {
                                bleResponseLabel.value = "心率值:${it.dataList[0]},当前时间：${formatted}"
                                healthData = "心率值:${it.dataList[0]},当前时间：${formatted}"
                            }
                            0x66 -> {
                                bleResponseLabel.value = "压力值:${it.dataList[0]},当前时间：${formatted}"
                                healthData = "压力值:${it.dataList[0]},当前时间：${formatted}"
                            }
                            else -> {
                                // 处理其他类型数据
                            }
                        }
                        // 将健康数据发送到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "Health Measurement Data",
                            content = healthData,
                            status = "publish"
                        )
                    } else {
                        bleResponseLabel.value = "測量失敗"
                    }
                }, {
                    Log.e("BleViewModel", "Error in healthMeasureStream: ${it.message}")
                }).let {}

            StarmaxBleClient.instance.nfcM1Stream()
                .takeUntil(disconnectSubject)
                .subscribe({
                    var waitM1DataList = it.waitM1DataList
                    bleResponseLabel.value = "获取到NFC待破解数据"

                    StarmaxBleClient.instance.nfcM1Ack().subscribe({
                        bleResponseLabel.value = "获取到NFC待破解数据:" + byteArrayToHexString(waitM1DataList.map { it.toByte() }.toByteArray()) + ",已应答"

                        // 将 NFC 数据发送到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "NFC Data",
                            content = "获取到NFC待破解数据: " + byteArrayToHexString(waitM1DataList.map { it.toByte() }.toByteArray()),
                            status = "publish"
                        )
                    }, {}).let {}

                    object : Thread() {
                        override fun run() {
                            println(byteArrayToHexString(it.waitM1DataList.map { it.toByte() }.toByteArray()))

                            CrackRepository.m1(byteArrayToHexString(it.waitM1DataList.map { it.toByte() }.toByteArray()), onSuccess = { crackData, _ ->
                                bleResponseLabel.value = "获取到NFC待破解数据,破解完成"

                                if (crackData != null) {
                                    StarmaxBleClient.instance.nfcM1Result(true, hexStringToByteArray(crackData.crackData)).subscribe({
                                        bleResponseLabel.value = "获取到NFC待破解数据,破解完成,已回复" + byteArrayToHexString(hexStringToByteArray(crackData.crackData))

                                        // 将破解后的 NFC 数据发送到 WordPress
                                        val wordpressService = WordpressApiService()
                                        wordpressService.sendBluetoothDataToWordpress(
                                            title = "NFC Crack Data",
                                            content = "破解完成，数据: " + byteArrayToHexString(hexStringToByteArray(crackData.crackData)),
                                            status = "publish"
                                        )
                                    }, {}).let {}
                                }
                            }, onError = { e ->
                                e?.printStackTrace()
                            })
                        }
                    }.start()
                }, {
                    Log.e("BleViewModel", "Error in NFC stream: ${it.message}")
                }).let {}
        }
    }

    fun byteArrayToHexString(data:ByteArray) : String {
        val bytes = data
        val stringBuilder = StringBuilder()

        for (i in bytes.indices) {
            stringBuilder.append(String.format("%02X", bytes[i])) // 将字节转换为十六进制字符串
        }
        return stringBuilder.toString()
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            val hex = hexString.substring(i, i + 2)
            data[i / 2] = hex.toInt(16).toByte()
        }
        return data
    }

    fun pair() {
        StarmaxBleClient.instance.pair().subscribe({
            val pairStatusMessage = "佩戴状态:" + it.pairStatus
            bleResponseLabel.value = pairStatusMessage

            // 将佩戴状态发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Pairing Status",
                content = pairStatusMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in pairing: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getBtStatus() {
        Utils.p(StarmaxSend().getBtStatus())

        StarmaxBleClient.instance.getBtStatus().subscribe({
            val btStatusMessage = "bt状态:" + it.btStatus
            bleResponseLabel.value = btStatusMessage

            // 将蓝牙状态发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Bluetooth Status",
                content = btStatusMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting Bluetooth status: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun findDevice(isFind: Boolean) {
        StarmaxBleClient.instance.findDevice(isFind = isFind).subscribe({
            val findDeviceMessage = "查找手环成功"
            bleResponseLabel.value = findDeviceMessage

            // 将查找手环状态发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Find Device Status",
                content = findDeviceMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in finding device: ${it.message}")
        }).let {

        }
    }


    fun getPower() {
        StarmaxBleClient.instance.getPower().subscribe({
            val powerStatusMessage = ("电量:${it.power}\n"
                    + "是否充电:${it.isCharge}")
            bleResponseLabel.value = powerStatusMessage

            // 将电量信息发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Power Status",
                content = powerStatusMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting power status: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getVersion() {
        val calendar = Calendar.getInstance()
        val lastMills = calendar.timeInMillis
        StarmaxBleClient.instance.getVersion().subscribe({
            val currentCalendar = Calendar.getInstance()
            bleModel = it.model
            bleVersion = it.version
            bleUiVersion = it.uiVersion
            uiSupportDifferentialUpgrade = it.uiSupportDifferentialUpgrade

            val versionInfo = ("固件版本:${bleVersion}\n"
                    + "ui版本:${bleUiVersion}\n"
                    + "设备接收buf大小:${it.bufferSize}\n"
                    + "lcd宽:${it.lcdWidth}\n"
                    + "lcd高:${it.lcdHeight}\n"
                    + "屏幕类型:${it.screenType}\n"
                    + "设备型号:${bleModel}\n"
                    + "ui是否强制升级:${it.uiForceUpdate}\n"
                    + "是否支持差分升级:${uiSupportDifferentialUpgrade}\n"
                    + "是否支持血糖:${it.supportSugar}\n"
                    + "设备协议版本:${it.protocolVersion}\n"
                    + "app协议版本:${StarmaxSend().version()}\n"
                    + "是否支持新睡眠:${it.sleepVersion}\n"
                    + "耗时：${currentCalendar.timeInMillis - lastMills}")

            bleResponseLabel.value = versionInfo

            // 将版本信息发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Device Version Information",
                content = versionInfo,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting version: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setTimeOffset() {
        StarmaxBleClient.instance.setTimeOffset().subscribe({
            val timeOffsetResult = if (it.status == 0) {
                "设置时区成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = timeOffsetResult

            // 将时区设置结果发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Time Offset Setting",
                content = timeOffsetResult,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in setting time offset: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }


    fun getTimeOffset() {
        StarmaxBleClient.instance.getTimeOffset().subscribe({
            val timeOffsetResult = if (it.status == 0) {
                "获取时区成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = timeOffsetResult

            // 将时区获取结果发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Time Offset Retrieval",
                content = timeOffsetResult,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting time offset: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getHealthDetail() {
        StarmaxBleClient.instance.getHealthDetail().subscribe({
            val healthDetailResult = if (it.status == 0) {
                ("总的计步值:${it.totalSteps}\n"
                        + "总的卡路里(卡):${it.totalHeat}\n"
                        + "总的距离(m):${it.totalDistance}\n"
                        + "睡眠总时间(分钟):${it.totalSleep}\n"
                        + "深睡时间:${it.totalDeepSleep}\n"
                        + "浅睡时间:${it.totalLightSleep}\n"
                        + "当前心率:${it.currentHeartRate}\n"
                        + "当前血压:${it.currentSs} /${it.currentFz}\n"
                        + "当前血氧:${it.currentBloodOxygen}\n"
                        + "当前压力:${it.currentPressure}\n"
                        + "当前MAI:${it.currentMai}\n"
                        + "当前梅脱:${it.currentMet}\n"
                        + "当前温度:${it.currentTemp}\n"
                        + "当前血糖:${it.currentBloodSugar}\n"
                        + "是否佩戴${it.isWear}\n")
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = healthDetailResult

            // 将健康数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Health Details",
                content = healthDetailResult,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting health details: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getClock() {
        StarmaxBleClient.instance.getClock().subscribe({
            val clockDataResult = if (it.status == 0) {
                it.toString() // 将时钟数据转换为字符串
            } else {
                statusLabel(it.status) // 如果有错误，获取错误状态信息
            }

            bleResponseLabel.value = clockDataResult

            // 将时钟数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Clock Data",
                content = clockDataResult,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting clock data: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setClock() {
        StarmaxBleClient.instance.setClock(
            clocks = arrayListOf(
                Clock(9, 0, true, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0),
                Clock(11, 45, true, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0),
                Clock(18, 0, false, intArrayOf(1, 1, 0, 1, 0, 1, 0), 0)
            )
        ).subscribe({
            val clockSetResult = if (it.status == 0) {
                "设置闹钟成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = clockSetResult

            // 将设置结果发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Clock Set Result",
                content = clockSetResult,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in setting clock: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getLongSit() {
        StarmaxBleClient.instance.getLongSit().subscribe({
            val longSitData = if (it.status == 0) {
                it.toString()
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = longSitData

            // 将久坐提醒数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Long Sit Data",
                content = longSitData,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting long sit data: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setLongSit() {
        StarmaxBleClient.instance.setLongSit(
            true,
            9,
            0,
            23,
            0,
            1
        ).subscribe({
            val setLongSitMessage = if (it.status == 0) {
                "设置久坐成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = setLongSitMessage

            // 将设置久坐提醒的数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Set Long Sit",
                content = setLongSitMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in setting long sit: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getDrinkWater() {
        StarmaxBleClient.instance.getDrinkWater().subscribe({
            val drinkWaterMessage = if (it.status == 0) {
                it.toString()  // 获取喝水提醒成功时的详细信息
            } else {
                statusLabel(it.status)  // 获取失败时的错误信息
            }

            bleResponseLabel.value = drinkWaterMessage

            // 将获取喝水提醒的数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Get Drink Water Reminder",
                content = drinkWaterMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting drink water reminder: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setDrinkWater() {
        StarmaxBleClient.instance.setDrinkWater(
            true, // 是否启用喝水提醒
            9,    // 开始时间：小时
            0,    // 开始时间：分钟
            23,   // 结束时间：小时
            0,    // 结束时间：分钟
            1     // 提醒间隔（单位：小时）
        ).subscribe({
            val drinkWaterSetMessage = if (it.status == 0) {
                "设置喝水成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = drinkWaterSetMessage

            // 将设置喝水提醒的结果发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Set Drink Water Reminder",
                content = drinkWaterSetMessage,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in setting drink water reminder: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun sendMessage() {
        StarmaxBleClient.instance.sendMessage(
            MessageType.Other, "新消息", "新消息内容"
        ).subscribe({
            val messageSentStatus = if (it.status == 0) {
                "发送消息成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = messageSentStatus

            // 将发送消息的结果发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Message Sent Status",
                content = messageSentStatus,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in sending message: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setWeather() {
        StarmaxBleClient.instance.setWeather(
            arrayListOf(
                WeatherDay(-9, 40, -20, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x01),
                WeatherDay(-10, 0, -16, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x05),
                WeatherDay(-11, 0, -10, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06),
                WeatherDay(-12, 35, 19, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x12)
            )
        ).subscribe({
            val weatherUpdateStatus = if (it.status == 0) {
                "设置天气成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = weatherUpdateStatus

            // 将天气设置结果发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Weather Update Status",
                content = weatherUpdateStatus,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in setting weather: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getWeatherSeven() {
        StarmaxBleClient.instance.getWeatherSeven().subscribe({
            val weatherFetchStatus = if (it.status == 0) {
                "读取天气成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = weatherFetchStatus

            // 构造天气信息的 JSON 格式
            val result = WeatherSevenFactory().buildGetMap(it)
            bleResponse.value = JSONObject(result.obj).toString()

            // 将天气信息发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Weather Data",
                content = bleResponse.value,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in fetching weather data: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setWeatherSeven() {
        StarmaxBleClient.instance.setWeatherSeven(
            cityName = "深圳",
            arrayListOf(
                WeatherDay(-10, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
                WeatherDay(0x1b, 0x23, 0x13, 0x05, 0x25, 0x0a, 0x07, 0x01, 0x06, 0, 0, 23, 59, 0, 0, 23, 59),
            )
        ).subscribe({
            val weatherSetStatus = if (it.status == 0) {
                "设置天气成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = weatherSetStatus

            // 构造天气信息的 JSON 数据
            val weatherData = JSONObject(mapOf(
                "city" to "深圳",
                "weather_days" to listOf(
                    mapOf("min" to -10, "max" to 0x23, "day_condition" to 0x13, "night_condition" to 0x05),
                    mapOf("min" to 0x1b, "max" to 0x23, "day_condition" to 0x13, "night_condition" to 0x05)
                    // 添加其他天数的天气信息
                )
            )).toString()

            // 将天气信息发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Weather Seven Data",
                content = weatherData,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in setting weather data: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getSummerWorldClock() {
        StarmaxBleClient.instance.getSummerWorldClock().subscribe({
            val clockStatus = if (it.status == 0) {
                "读取世界时钟成功"
            } else {
                statusLabel(it.status)
            }

            bleResponseLabel.value = clockStatus

            // 构造世界时钟信息的 JSON 数据
            val result = SummerWorldClockFactory().buildGetMap(it)
            val clockData = JSONObject(result.obj).toString()

            bleResponse.value = clockData

            // 将世界时钟信息发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Summer World Clock Data",
                content = clockData,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in getting summer world clock: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun sendMusic() {
        StarmaxBleClient.instance.musicControl(
            1, 20, 30, "kasd asssssa", "adsadsd"
        ).subscribe({
            bleResponseLabel.value = "音乐控制成功"

            // 构造音乐控制的 JSON 数据
            val musicControlData = JSONObject(mapOf(
                "action" to "music_control",
                "volume" to 20,
                "track_duration" to 30,
                "track_title" to "kasd asssssa",
                "artist" to "adsadsd"
            )).toString()

            // 将音乐控制信息发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Music Control",
                content = musicControlData,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error in music control: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getReminder() {
        StarmaxBleClient.instance.getEventReminder().subscribe({
            if (it.status == 0) {
                var str = ""

                val reminderList = it.eventRemindersList
                val reminderData = mutableListOf<Map<String, Any>>() // 用于构造 JSON 数据

                for (i in reminderList.indices) {
                    val oneData = reminderList[i]
                    str += ("时间:" + oneData.year + "-" + oneData.month + "-" + oneData.day + oneData.hour + ":" + oneData.minute
                            + " 内容: " + oneData.content + "\n")

                    // 构造每个提醒项的 JSON 数据
                    val reminderMap = mapOf(
                        "year" to oneData.year,
                        "month" to oneData.month,
                        "day" to oneData.day,
                        "hour" to oneData.hour,
                        "minute" to oneData.minute,
                        "content" to oneData.content
                    )
                    reminderData.add(reminderMap)
                }

                bleResponseLabel.value = str

                // 构造完整的 JSON 数据
                val reminderJson = JSONObject(mapOf("reminders" to reminderData)).toString()

                // 将提醒信息发送到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Event Reminders",
                    content = reminderJson,
                    status = "publish"
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error retrieving reminders: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setReminder() {
        val calendar = Calendar.getInstance()
        val eventReminder = EventReminder(
            calendar,
            "和朋友出去旅游",
            1,
            3,
            intArrayOf(0, 0, 0, 1, 0, 0, 0)
        )

        StarmaxBleClient.instance.setEventReminder(listOf(eventReminder)).subscribe({
            if (it.status == 0) {
                // 设置提醒成功后的消息
                bleResponseLabel.value = "设置提醒成功: $eventReminder"

                // 构造提醒数据的 JSON
                val reminderJson = JSONObject(mapOf(
                    "title" to "和朋友出去旅游",
                    "reminder_type" to 1,
                    "reminder_priority" to 3,
                    "repeat_days" to intArrayOf(0, 0, 0, 1, 0, 0, 0).joinToString()
                )).toString()

                // 将提醒信息发送到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "New Event Reminder",
                    content = reminderJson,
                    status = "publish"
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error setting reminder: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getSportMode() {
        StarmaxBleClient.instance.getSportMode().subscribe({
            if (it.status == 0) {
                var str = ""
                val sportModes = mutableListOf<Map<String, Any>>()

                val dataList = it.sportModesList
                for (i in 0 until dataList.size) {
                    val modeLabel = sportModeLabel(dataList[i])
                    str += "运动模式: $modeLabel\n"

                    // 将运动模式添加到列表中
                    sportModes.add(mapOf("mode" to modeLabel))
                }

                bleResponseLabel.value = str

                // 构造运动模式的 JSON
                val sportModesJson = JSONObject(mapOf(
                    "sport_modes" to sportModes
                )).toString()

                // 将运动模式信息发送到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Sport Modes Data",
                    content = sportModesJson,
                    status = "publish"
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error fetching sport modes: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun setSportMode() {
        val sportModes = listOf(
            0x0A,
            0x0B,
            0x0C,
            0x0D
        )

        StarmaxBleClient.instance.setSportMode(sportModes).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "运动模式设置成功: $sportModes"

                // 构造运动模式的 JSON
                val sportModesJson = JSONObject(mapOf(
                    "set_sport_modes" to sportModes.map { sportModeLabel(it) }
                )).toString()

                // 将设置的运动模式数据发送到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Set Sport Modes",
                    content = sportModesJson,
                    status = "publish"
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error setting sport modes: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun sendHealthMeasure(isOpen: Boolean) {
        StarmaxBleClient.instance.sendHealthMeasure(HistoryType.Pressure, isOpen).subscribe({
            val responseMessage = if (isOpen) "压力测量开启成功" else "压力测量关闭成功"
            bleResponseLabel.value = responseMessage

            // 构造 JSON 数据
            val healthMeasureData = JSONObject(mapOf(
                "measure_type" to "Pressure",
                "status" to if (isOpen) "enabled" else "disabled"
            )).toString()

            // 将数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Pressure Health Measure Status",
                content = healthMeasureData,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error setting health measure: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun sendHeartRateHealthMeasure(isOpen: Boolean) {
        StarmaxBleClient.instance.sendHealthMeasure(HistoryType.HeartRate, isOpen).subscribe({
            val responseMessage = if (isOpen) "心率测量开启成功" else "心率测量关闭成功"
            bleResponseLabel.value = responseMessage

            // 构造 JSON 数据
            val heartRateMeasureData = JSONObject(mapOf(
                "measure_type" to "Heart Rate",
                "status" to if (isOpen) "enabled" else "disabled"
            )).toString()

            // 将数据发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Heart Rate Health Measure Status",
                content = heartRateMeasureData,
                status = "publish"
            )
        }, {
            Log.e("BleViewModel", "Error setting heart rate measure: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getDebugInfo(fileType: Int) {
        StarmaxBleClient.instance.getDebugInfo(packageId, fileType).subscribe({
            if (it.status == 0) {
                if (it.dataList.size > 0) {
                    val debugData = if (fileType == 3) {
                        it.dataList.map { it.toByte() }.toByteArray().toString(Charsets.US_ASCII).replace("TAG=", "\nTAG=")
                    } else {
                        it.dataList.map { String.format("0x%02X", it.toByte()) }.toList().chunked(4)
                            .map { chunk -> chunk.joinToString(",") }.joinToString(",\n") + ",\n\n\n"
                    }

                    // 本地存储调试信息
                    TestRepository.testLocal(
                        localBasePath,
                        debugData,
                        when (fileType) {
                            1 -> "battery.txt"
                            2 -> "gsensor.txt"
                            3 -> "sleep.txt"
                            else -> "debug.txt"
                        }
                    )

                    packageId += 1
                    getDebugInfo(fileType)

                    // 上传调试数据到 WordPress
                    val debugInfo = JSONObject(mapOf(
                        "package_id" to packageId,
                        "file_type" to when (fileType) {
                            1 -> "Battery"
                            2 -> "Gsensor"
                            3 -> "Sleep"
                            else -> "Debug"
                        },
                        "debug_data" to debugData
                    )).toString()

                    val wordpressService = WordpressApiService()
                    wordpressService.sendBluetoothDataToWordpress(
                        title = "Debug Info - Package $packageId",
                        content = debugInfo,
                        status = "publish"
                    )
                }
                bleResponseLabel.value = "获取" + (if (fileType == 1) "battery.txt" else "gsensor.txt") + "第" + packageId + "包"
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting debug info: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getSportHistory() {
        StarmaxBleClient.instance.getSportHistory().subscribe({
            if (it.status == 0) {
                val sportHistoryJson = SportHistoryFactory(StarmaxBleClient.instance.bleNotify).buildMapFromProtobuf(it).toJson()
                bleResponseLabel.value = sportHistoryJson

                // 上传运动历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Sport History Data",  // 设置标题
                    content = sportHistoryJson,    // 发送的运动历史数据作为文章内容
                    status = "publish"             // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting sport history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getStepHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getStepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val stepList = it.stepsList
                for (i in 0 until stepList.size) {
                    val oneData = stepList[i]
                    str += ("时间:" + oneData.hour + ":" + oneData.minute
                            + " 步数" + oneData.steps
                            + ",卡路里" + ((oneData.calorie).toDouble() / 1000) + "千卡"
                            + ",距离" + ((oneData.distance).toDouble() / 100) + "米\n")
                }

                val sleepList = it.sleepsList
                for (i in 0 until sleepList.size) {
                    val oneData = sleepList[i]
                    str += ("时间:" + oneData.hour + ":" + oneData.minute
                            + " 睡眠状态" + oneData.sleepStatus + "\n")
                }

                bleResponseLabel.value = str

                // 上传步数历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Step History Data",  // 设置标题
                    content = str,               // 步数历史数据作为内容
                    status = "publish"           // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting step history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getBloodPressureHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getBloodPressureHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 伸缩压" + oneData.ss + " 舒张压" + oneData.fz + "\n"
                }

                bleResponseLabel.value = str

                // 上传血压历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Blood Pressure History Data",  // 设置标题
                    content = str,                          // 血压历史数据作为内容
                    status = "publish"                      // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting blood pressure history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getHeartRateHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getHeartRateHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 心率" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str

                // 上传心率历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Heart Rate History Data",  // 设置标题
                    content = str,                      // 心率历史数据作为内容
                    status = "publish"                  // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting heart rate history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getBloodOxygenHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getBloodOxygenHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 血氧" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str

                // 上传血氧历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Blood Oxygen History Data",  // 设置标题
                    content = str,                       // 血氧历史数据作为内容
                    status = "publish"                   // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting blood oxygen history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getPressureHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getPressureHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 压力" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str

                // 上传压力历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Pressure History Data",  // 设置标题
                    content = str,                    // 压力历史数据作为内容
                    status = "publish"                // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting pressure history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getMetHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getMetHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "梅脱:" + oneData + "\n"
                }

                bleResponseLabel.value = str

                // 上传MET历史数据到WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "MET History Data",  // 设置标题
                    content = str,               // MET历史数据作为内容
                    status = "publish"           // 发布状态
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting MET history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getOriginSleepHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getOriginSleepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    if(oneData.value > 0){
                        val valueList = Utils.int2byte(oneData.value, 2)
                        str += "时间:" + oneData.hour + ":" + oneData.minute + " 红外:" + valueList[1] + " sar:" + (valueList[0] * 256) + "\n"
                    }
                }

                bleResponseLabel.value = str

                // 上传原始睡眠历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Origin Sleep History Data",  // 设置标题
                    content = str,                         // 原始睡眠历史数据作为内容
                    status = "publish"                     // 发布状态
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting Origin Sleep history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getTempHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getTempHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 温度" + oneData.value + "%\n"
                }

                bleResponseLabel.value = str

                // 上传温度历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Temperature History Data",  // 设置标题
                    content = str,                       // 温度历史数据作为内容
                    status = "publish"                   // 发布状态
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting Temperature history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getMaiHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getMaiHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "MAI:" + oneData
                }

                bleResponseLabel.value = str

                // 上传 MAI 历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "MAI History Data",  // 设置标题
                    content = str,               // MAI 历史数据作为内容
                    status = "publish"           // 发布状态
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting MAI history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getBloodSugarHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getBloodSugarHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    str += "时间:" + oneData.hour + ":" + oneData.minute + " 血糖" + oneData.value + "\n"
                }

                bleResponseLabel.value = str

                // 上传血糖历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Blood Sugar History Data",  // 设置标题
                    content = str,                       // 血糖历史数据作为内容
                    status = "publish"                   // 发布状态
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting Blood Sugar history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getSleepHistory(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        StarmaxBleClient.instance.getSleepHistory(calendar).subscribe({
            if (it.status == 0) {
                var str = ("采样间隔:" + it.interval + "分钟\n"
                        + "日期:" + it.year + "-" + it.month + "-" + it.day + "\n"
                        + "数据长度:" + it.dataLength + "\n"
                        )

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    if(oneData.status != 0){
                        str += "时间:" + oneData.hour + ":" + oneData.minute + " 状态" + oneData.status + "\n"
                    }
                }

                bleResponseLabel.value = str

                // 上传睡眠历史数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Sleep History Data",  // 设置标题
                    content = str,                 // 睡眠历史数据作为内容
                    status = "publish"             // 发布状态
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting Sleep history: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun getValidHistoryDates() {
        getValidHistoryDates(HistoryType.Step)
    }

    fun getSleepValidHistoryDates() {
        getValidHistoryDates(HistoryType.Sleep)
    }

    fun getMetValidHistoryDates() {
        getValidHistoryDates(HistoryType.Met)
    }

    fun getMaiValidHistoryDates() {
        getValidHistoryDates(HistoryType.Mai)
    }

    fun getBloodSugarValidHistoryDates() {
        getValidHistoryDates(HistoryType.BloodSugar)
    }

    fun getBloodOxygenValidHistoryDates() {
        getValidHistoryDates(HistoryType.BloodOxygen)
    }

    fun getValidHistoryDates(historyType: HistoryType) {
        StarmaxBleClient.instance.getValidHistoryDates(historyType).subscribe({
            if (it.status == 0) {
                var str = "有效日期\n"

                val dataList = it.dataList
                for (i in 0 until dataList.size) {
                    val oneData = dataList[i]
                    val year = oneData.year
                    val month = oneData.month
                    val day = oneData.day
                    str += "$year-$month-$day\n"
                }

                bleResponseLabel.value = str

                // 上传有效历史日期数据到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "${historyType.name} Valid History Dates",  // 设置标题
                    content = str,                                      // 有效历史日期作为内容
                    status = "publish"                                  // 发布状态
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {
            Log.e("BleViewModel", "Error getting valid history dates: ${it.message}")
        }).let {
            sendDisposable.add(it)
        }
    }

    fun sendUi() {
        object : Thread() {
            override fun run() {
                UiRepository.getVersion(
                    model = bleModel,
                    version = bleUiVersion,
                    onSuccess = { ui, _ ->
                        if (ui == null) {
                            return@getVersion
                        }

                        val file = File(savePath)
                        if (!file.exists()) file.mkdirs()
                        val url = ui.binUrl
                        val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

                        val apkFile = File(savePath + saveName)
                        if (apkFile.exists()) apkFile.delete()
                        object : Thread() {
                            override fun run() {
                                try {
                                    NetFileUtils.downloadUpdateFile(url, apkFile) {
                                        changeMtu {
                                            try {
                                                val fis = FileInputStream(apkFile)
                                                BleFileSender.initFile(fis,
                                                    object : BleFileSenderListener() {
                                                        override fun onSuccess() {
                                                            bleMessage.value = "UI发送成功"
                                                            // 上传消息到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Installation Success",
                                                                content = "UI已成功安装",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onProgress(progress: Double) {
                                                            bleMessage.value = "当前进度${progress}%"
                                                            // 上传进度到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Installation Progress",
                                                                content = "UI安装进度: ${progress}%",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onFailure(status: Int) {
                                                            bleMessage.value = "安装失败"
                                                            // 上传失败消息到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Installation Failed",
                                                                content = "安装失败, 错误码: $status",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onStart() {
                                                            val data = StarmaxSend().sendUi(offset = 0, ui.version)
                                                            sendMsg(data)
                                                            // 上传开始安装消息到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Installation Started",
                                                                content = "开始安装UI版本: ${ui.version}",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onCheckSum() {
                                                            // 处理校验和（如果需要）
                                                        }

                                                        override fun onSendComplete() {
                                                            // 处理发送完成后的逻辑（如果需要）
                                                        }

                                                        override fun onSend() {
                                                            if (BleFileSender.hasNext()) {
                                                                val data = StarmaxSend().sendFile()
                                                                sendMsg(data)
                                                            }
                                                        }
                                                    })

                                                BleFileSender.sliceBuffer = 8
                                                BleFileSender.onStart()
                                            } catch (e: FileNotFoundException) {
                                                bleMessage.value = "文件未找到"
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: java.lang.Exception) {
                                    bleMessage.value = "服务器错误"
                                    e.printStackTrace()
                                }
                            }
                        }.start()
                    },
                    onError = { e ->
                        bleMessage.value = "服务器错误"
                        e?.printStackTrace()
                        // 上传服务器错误消息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "UI Installation Error",
                            content = "获取UI版本时发生错误: ${e?.message}",
                            status = "publish"
                        )
                    })
            }
        }.start()
    }

    fun sendUiDiff() {
        if (!uiSupportDifferentialUpgrade) {
            bleMessage.value = "当前设备不支持UI差分升级"
            // 发送到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "UI Differential Upgrade Unsupported",
                content = "设备不支持UI差分升级",
                status = "publish"
            )
            return
        }

        object : Thread() {
            override fun run() {
                UiRepository.getDiff(
                    model = bleModel,
                    version = bleUiVersion,
                    onSuccess = { ui, _ ->
                        if (ui == null) {
                            return@getDiff
                        }

                        val file = File(savePath)
                        if (!file.exists()) file.mkdirs()
                        val url = ui.binUrl
                        val saveName = url.substring(url.lastIndexOf('/') + 1, url.length)

                        val apkFile = File(savePath + saveName)
                        if (apkFile.exists()) apkFile.delete()
                        object : Thread() {
                            override fun run() {
                                try {
                                    NetFileUtils.downloadUpdateFile(url, apkFile) {
                                        changeMtu {
                                            try {
                                                val fis = FileInputStream(apkFile)

                                                BleFileSender.initFile(fis,
                                                    object : BleFileSenderListener() {
                                                        override fun onSuccess() {
                                                            bleMessage.value = "差分升级成功"
                                                            // 发送到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Differential Upgrade Success",
                                                                content = "差分升级成功",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onProgress(progress: Double) {
                                                            bleMessage.value = "当前进度${progress}%"
                                                            // 发送到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Differential Upgrade Progress",
                                                                content = "差分升级进度: ${progress}%",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onFailure(status: Int) {
                                                            bleMessage.value = "差分升级失败"
                                                            // 发送失败到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Differential Upgrade Failed",
                                                                content = "差分升级失败，错误码: $status",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onStart() {
                                                            val data = StarmaxSend().sendUi(offset = ui.offset, ui.version)
                                                            sendMsg(data)
                                                            // 发送开始到 WordPress
                                                            val wordpressService = WordpressApiService()
                                                            wordpressService.sendBluetoothDataToWordpress(
                                                                title = "UI Differential Upgrade Started",
                                                                content = "开始差分升级，UI版本: ${ui.version}",
                                                                status = "publish"
                                                            )
                                                        }

                                                        override fun onCheckSum() {
                                                            // 处理校验和
                                                        }

                                                        override fun onSendComplete() {
                                                            // 完成发送
                                                        }

                                                        override fun onSend() {
                                                            if (BleFileSender.hasNext()) {
                                                                val data = StarmaxSend().sendFile()
                                                                sendMsg(data)
                                                            }
                                                        }
                                                    })

                                                BleFileSender.sliceBuffer = 8
                                                BleFileSender.onStart()
                                            } catch (e: FileNotFoundException) {
                                                bleMessage.value = "文件未找到"
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    bleMessage.value = "服务器错误"
                                    e.printStackTrace()
                                    // 发送服务器错误到 WordPress
                                    val wordpressService = WordpressApiService()
                                    wordpressService.sendBluetoothDataToWordpress(
                                        title = "UI Differential Upgrade Server Error",
                                        content = "服务器错误: ${e.message}",
                                        status = "publish"
                                    )
                                }
                            }
                        }.start()
                    },
                    onError = { e ->
                        bleMessage.value = "服务器错误"
                        e?.printStackTrace()
                        // 发送错误消息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "UI Differential Upgrade Error",
                            content = "获取UI差分版本时发生错误: ${e?.message}",
                            status = "publish"
                        )
                    }
                )
            }
        }.start()
    }

    fun sendUiLocal(context: Context, uri: Uri) {
        try {
            val fis = context.contentResolver.openInputStream(uri)

            BleFileSender.initFile(fis,
                object : BleFileSenderListener() {
                    override fun onSuccess() {
                        bleMessage.value = "UI升级成功"
                        // 发送成功信息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "UI Local Upgrade Success",
                            content = "UI本地升级成功",
                            status = "publish"
                        )
                    }

                    override fun onProgress(progress: Double) {
                        bleMessage.value = "当前进度${progress}%"
                        // 发送进度信息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "UI Local Upgrade Progress",
                            content = "UI本地升级进度: ${progress}%",
                            status = "publish"
                        )
                    }

                    override fun onCheckSum() {
                        // 校验和处理
                    }

                    override fun onSendComplete() {
                        // 发送完成后的操作
                    }

                    override fun onFailure(status: Int) {
                        bleMessage.value = "UI升级失败"
                        // 发送失败信息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "UI Local Upgrade Failed",
                            content = "UI本地升级失败，错误码: $status",
                            status = "publish"
                        )
                    }

                    override fun onStart() {
                        val data = StarmaxSend().sendUi(offset = 0, "1.0.0")
                        sendMsg(data)
                        // 发送开始信息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "UI Local Upgrade Started",
                            content = "开始UI本地升级，版本: 1.0.0",
                            status = "publish"
                        )
                    }

                    override fun onSend() {
                        if (BleFileSender.hasNext()) {
                            val data = StarmaxSend().sendFile()
                            sendMsg(data)
                        }
                    }
                })

            BleFileSender.sliceBuffer = 8
            BleFileSender.onStart()

        } catch (e: FileNotFoundException) {
            bleMessage.value = "未找到文件"
            e.printStackTrace()
            // 发送错误信息到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "UI Local Upgrade Error",
                content = "未找到文件: ${e.message}",
                status = "publish"
            )
        }
    }

    fun sendDialLocal(context: Context) {
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                var lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {
                            bleMessage.value = "表盘设置成功"
                            // 发送成功信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Dial Setting Success",
                                content = "表盘设置成功",
                                status = "publish"
                            )
                        }

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"
                            // 发送进度信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Dial Setting Progress",
                                content = "表盘设置进度: ${progress}%",
                                status = "publish"
                            )
                        }

                        override fun onFailure(status: Int) {
                            bleMessage.value = "表盘设置失败，错误码: $status"
                            // 发送失败信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Dial Setting Failed",
                                content = "表盘设置失败，错误码: $status",
                                status = "publish"
                            )
                        }

                        override fun onCheckSum() {
                            // 校验和处理
                        }

                        override fun onSendComplete() {
                            // 发送完成后的操作
                        }

                        override fun onStart() {
                            val data = StarmaxSend()
                                .sendDial(
                                    5001,
                                    BmpUtils.bmp24to16(255, 255, 255),
                                    1
                                )
                            sendMsg(data)
                            // 发送开始信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Dial Setting Started",
                                content = "表盘设置已开始",
                                status = "publish"
                            )
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8
                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误: ${e.message}"
                e.printStackTrace()
                // 发送错误信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Dial Setting Error",
                    content = "服务器错误: ${e.message}",
                    status = "publish"
                )
            }
        }
    }

    fun sendLogoLocal(context: Context) {
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {
                            bleMessage.value = "Logo发送成功"
                            // 发送成功信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Logo Upload Success",
                                content = "Logo上传成功",
                                status = "publish"
                            )
                        }

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"
                            // 发送进度信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Logo Upload Progress",
                                content = "Logo上传进度: ${progress}%",
                                status = "publish"
                            )
                        }

                        override fun onFailure(status: Int) {
                            bleMessage.value = "Logo发送失败，错误码: $status"
                            // 发送失败信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Logo Upload Failed",
                                content = "Logo发送失败，错误码: $status",
                                status = "publish"
                            )
                        }

                        override fun onCheckSum() {
                            // 校验和处理
                        }

                        override fun onSendComplete() {
                            // 完成后的操作
                        }

                        override fun onStart() {
                            val data = StarmaxSend().sendLogo()
                            sendMsg(data)
                            // 发送开始信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Logo Upload Started",
                                content = "Logo上传已开始",
                                status = "publish"
                            )
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                sendMsg(data)
                            }
                        }
                    })

                BleFileSender.sliceBuffer = 8
                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误: ${e.message}"
                e.printStackTrace()
                // 发送错误信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Logo Upload Error",
                    content = "服务器错误: ${e.message}",
                    status = "publish"
                )
            }
        }
    }

    fun clearLogo(){
        StarmaxBleClient.instance.clearLogo().subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "清除logo成功"
                // 发送成功信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Logo Clear Success",
                    content = "Logo清除成功",
                    status = "publish"
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)
                // 发送失败信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Logo Clear Failed",
                    content = "Logo清除失败，错误码: ${statusLabel(it.status)}",
                    status = "publish"
                )
            }
        }, {
            // 处理异常
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Logo Clear Error",
                content = "Logo清除时发生错误: ${it.message}",
                status = "publish"
            )
        }).let {
            sendDisposable.add(it)
        }
    }

    fun sendGts7FirmwareLocal(context: Context) {
        if (binUri == null) {
            return
        }

        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFile(
                    bin,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {
                            val currentCalendar = Calendar.getInstance()
                            val diffInSeconds: Long = (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                            bleMessage.value = "发送完成,耗时${diffInSeconds}"

                            // 发送成功信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "GTS7 Firmware Sent Successfully",
                                content = "固件发送完成, 耗时: ${diffInSeconds} 秒",
                                status = "publish"
                            )
                        }

                        override fun onProgress(progress: Double) {
                            val currentCalendar = Calendar.getInstance()
                            val diffInSeconds: Long = (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                            bleMessage.value = "当前进度${progress.toInt()}%,耗时${diffInSeconds}"

                            // 发送进度更新到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "GTS7 Firmware Update Progress",
                                content = "当前进度: ${progress.toInt()}%, 耗时: ${diffInSeconds} 秒",
                                status = "publish"
                            )
                        }

                        override fun onFailure(status: Int) {
                            bleMessage.value = "发送失败"

                            // 发送失败信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "GTS7 Firmware Send Failed",
                                content = "固件发送失败, 状态码: ${status}",
                                status = "publish"
                            )
                        }

                        override fun onCheckSum() {
                            val data = StarmaxSend().sendDiffCheckSum()
                            Log.d("Diff Sender", "${BleFileSender.checksumData.size}")
                            bleMessage.value = "正在发送第${BleFileSender.checksumSendIndex}包校验码"
                            sendMsg(data)
                        }

                        override fun onStart() {
                            val data = StarmaxSend().sendDiffHeader()
                            bleMessage.value = "发送文件头"
                            sendMsg(data)
                        }

                        override fun onSendComplete() {
                            val data = StarmaxSend().sendDiffComplete()
                            bleMessage.value = "发送结束通知固件"
                            sendMsg(data)
                        }

                        override fun onSend() {
                            val data = StarmaxSend().sendDiffFile()
                            sendMsg(data)
                        }
                    })

                BleFileSender.sliceBuffer = 8

                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()

                // 发送错误信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "GTS7 Firmware Send Error",
                    content = "服务器错误: 文件未找到",
                    status = "publish"
                )
            }
        }
    }

    fun sendGts7CrcLocal(context: Context) {
        if(binUri == null){
            return
        }

        try {
            val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
            val lastSendCalendar = Calendar.getInstance()
            BleFileSender.initFile(
                bin,
                object : BleFileSenderListener() {
                    override fun onSuccess() {
                        val currentCalendar = Calendar.getInstance()
                        val diffInSeconds: Long = (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                        bleMessage.value = "发送完成,耗时${diffInSeconds}"

                        // 上传成功信息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "GTS7 CRC Sent Successfully",
                            content = "CRC 发送完成, 耗时: ${diffInSeconds} 秒",
                            status = "publish"
                        )
                    }

                    override fun onProgress(progress: Double) {
                        val currentCalendar = Calendar.getInstance()
                        val diffInSeconds: Long = (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                        bleMessage.value = "当前进度${progress.toInt()}%,耗时${diffInSeconds}"

                        // 上传进度更新到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "GTS7 CRC Update Progress",
                            content = "当前进度: ${progress.toInt()}%, 耗时: ${diffInSeconds} 秒",
                            status = "publish"
                        )
                    }

                    override fun onFailure(status: Int) {
                        bleMessage.value = "发送失败"

                        // 上传失败信息到 WordPress
                        val wordpressService = WordpressApiService()
                        wordpressService.sendBluetoothDataToWordpress(
                            title = "GTS7 CRC Send Failed",
                            content = "CRC 发送失败, 状态码: ${status}",
                            status = "publish"
                        )
                    }

                    override fun onCheckSum() {
                        val data = StarmaxSend().sendDiffCheckSum()
                        Log.d("Diff Sender", "正在发送第${BleFileSender.checksumSendIndex}包")
                        bleMessage.value = "正在发送第${BleFileSender.checksumSendIndex}包校验码"
                        Utils.p(data)
                        StarmaxBleClient.instance.notify(StarmaxSendRequest(0xF3, intArrayOf(0x00, 0x01)).datas)
                    }

                    override fun onStart() {
                        val data = StarmaxSend().sendDiffHeader()
                        bleMessage.value = "发送文件头"
                        Utils.p(data)

                        StarmaxBleClient.instance.notify(StarmaxSendRequest(0xF3, intArrayOf(0x00, 0x00)).datas)
                    }

                    override fun onSendComplete() {
                        val data = StarmaxSend().sendDiffComplete()
                        bleMessage.value = "发送结束通知固件"
                        Utils.p(data)
                    }

                    override fun onSend() {
                        val data = StarmaxSend().sendDiffFile()
                        Utils.p(data)
                    }
                })

            BleFileSender.sliceBuffer = 8
            BleFileSender.onStart()
        } catch (e: FileNotFoundException) {
            bleMessage.value = "服务器错误"
            e.printStackTrace()

            // 上传错误信息到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "GTS7 CRC Send Error",
                content = "服务器错误: 文件未找到",
                status = "publish"
            )
        }
    }

    fun sendCustomDial(context: Context) {
        changeMtu {
            try {
                val bin = context.contentResolver.openInputStream(binUri!!) as FileInputStream?
                val img = context.contentResolver.openInputStream(imageUri!!) as FileInputStream?

                var lastSendCalendar = Calendar.getInstance()
                BleFileSender.initFileWithBackground(
                    bin,
                    240, 282,
                    img,
                    object : BleFileSenderListener() {
                        override fun onSuccess() {
                            val currentCalendar = Calendar.getInstance()
                            val diffInSeconds: Long = (currentCalendar.timeInMillis - lastSendCalendar.timeInMillis) / 1000
                            bleMessage.value = "发送完成,耗时${diffInSeconds}"

                            // 上传成功信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Custom Dial Sent Successfully",
                                content = "自定义表盘发送完成, 耗时: ${diffInSeconds} 秒",
                                status = "publish"
                            )
                        }

                        override fun onProgress(progress: Double) {
                            bleMessage.value = "当前进度${progress.toInt()}%"

                            // 上传进度更新到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Custom Dial Update Progress",
                                content = "当前进度: ${progress.toInt()}%",
                                status = "publish"
                            )
                        }

                        override fun onFailure(status: Int) {
                            bleMessage.value = "发送失败"

                            // 上传失败信息到 WordPress
                            val wordpressService = WordpressApiService()
                            wordpressService.sendBluetoothDataToWordpress(
                                title = "Custom Dial Send Failed",
                                content = "自定义表盘发送失败, 状态码: ${status}",
                                status = "publish"
                            )
                        }

                        override fun onStart() {
                            val data = StarmaxSend().sendDial(5001, BmpUtils.bmp24to16(255, 255, 255), 1)
                            Utils.p(data)
                            sendMsg(data)
                        }

                        override fun onCheckSum() {
                            // Checksum 逻辑可以在这里补充
                        }

                        override fun onSendComplete() {
                            // 完成发送时的逻辑可以在这里补充
                        }

                        override fun onSend() {
                            if (BleFileSender.hasNext()) {
                                val data = StarmaxSend().sendFile()
                                BleManager.getInstance().write(
                                    bleDevice?.get(),
                                    WriteServiceUUID.toString(),
                                    WriteCharacteristicUUID.toString(),
                                    data,
                                    object : BleWriteCallback() {
                                        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                                            if (current == total) {
                                                val newSendCalendar = Calendar.getInstance()
                                                val millis = newSendCalendar.timeInMillis - lastSendCalendar.timeInMillis
                                                Log.e("BleFileSender", "发送时间:${millis}, 当前rssi:")
                                                lastSendCalendar = Calendar.getInstance()
                                            }
                                        }

                                        override fun onWriteFailure(exception: BleException?) {
                                            bleMessage.value = "指令发送失败"
                                        }
                                    }
                                )
                            }
                        }
                    }
                )

                BleFileSender.sliceBuffer = 8
                BleFileSender.onStart()
            } catch (e: FileNotFoundException) {
                bleMessage.value = "服务器错误"
                e.printStackTrace()

                // 上传错误信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Custom Dial Send Error",
                    content = "服务器错误: 文件未找到",
                    status = "publish"
                )
            }
        }
    }

    fun getDialInfo() {
        StarmaxBleClient.instance.getDialInfo().subscribe({
            var str = ""

            val dataList = it.infosList
            for (i in 0 until dataList.size) {
                val oneData = dataList[i]
                val isSelected = oneData.isSelected
                val dialId = oneData.dialId
                val dialColor = oneData.dialColor
                val align = oneData.align
                if (isSelected == 1) {
                    str += "已选择\n"
                }
                str += "表盘id:${dialId}\n"
                str += "表盘颜色:${
                    Utils.bytesToHex(
                        Utils.int2byte(
                            dialColor,
                            3
                        )
                    )
                }\n"
                str += "位置:${align}\n"
            }

            bleResponseLabel.value = str

            // 上传表盘信息到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Dial Information",
                content = str,
                status = "publish"
            )

        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun switchDial() {
        StarmaxBleClient.instance.switchDial(5001).subscribe({
            if (it.status == 0) {
                bleResponseLabel.value = "切换表盘成功"

                // 上传切换表盘信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Dial Switched",
                    content = "表盘ID: 5001 切换成功",
                    status = "publish"
                )
            } else {
                bleResponseLabel.value = statusLabel(it.status)

                // 上传失败信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "Dial Switch Failed",
                    content = "表盘ID: 5001 切换失败, 状态: ${statusLabel(it.status)}",
                    status = "publish"
                )
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun reset() {
        StarmaxBleClient.instance.reset().subscribe({
            bleResponseLabel.value = "恢复出厂成功"

            // 上传恢复出厂信息到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Factory Reset",
                content = "设备已恢复出厂设置成功",
                status = "publish"
            )

        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun close() {
        StarmaxBleClient.instance.close().subscribe({
            bleResponseLabel.value = "关机成功"

            // 上传关机信息到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Device Shutdown",
                content = "设备已成功关机",
                status = "publish"
            )

        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun shippingMode() {
        StarmaxBleClient.instance.shippingMode().subscribe({
            bleResponseLabel.value = "进入船运模式"

            // 上传进入船运模式的信息到 WordPress
            val wordpressService = WordpressApiService()
            wordpressService.sendBluetoothDataToWordpress(
                title = "Shipping Mode Activated",
                content = "设备已成功进入船运模式",
                status = "publish"
            )

        }, {}).let {
            sendDisposable.add(it)
        }
    }

    fun getNfcCardInfo() {
        StarmaxBleClient.instance.getNfcInfo().subscribe({
            if (it.status == 0) {
                var str = ("类型:" + it.type)

                val cardsList = it.cardsList
                for (i in 0 until cardsList.size) {
                    val oneData = cardsList[i]
                    str += "卡片类型:" + oneData.cardType + ",卡片名称" + oneData.cardName + "%\n"
                }

                bleResponseLabel.value = str

                // 上传 NFC 信息到 WordPress
                val wordpressService = WordpressApiService()
                wordpressService.sendBluetoothDataToWordpress(
                    title = "NFC Card Information",
                    content = str,
                    status = "publish"
                )

            } else {
                bleResponseLabel.value = statusLabel(it.status)
            }
        }, {}).let {
            sendDisposable.add(it)
        }
    }

    /**
     * @param data
     */
    fun sendMsg(data: ByteArray?) {
        if (bleDevice == null || bleDevice!!.get() == null || !BleManager.getInstance()
                .isConnected(bleDevice!!.get())
        ) {
            sendDisposable.clear() //清空发送栈
            viewModelScope.launch {
                Toast.makeText(context, "蓝牙未连接", Toast.LENGTH_SHORT).show()
            }
            return
        }

        BleManager.getInstance().write(
            bleDevice?.get(),
            WriteServiceUUID.toString(),
            WriteCharacteristicUUID.toString(),
            data,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    //bleMessage.value = "指令发送成功"
                    //println("当前 $current 总共 $total 已写 $justWrite")
                }

                override fun onWriteFailure(exception: BleException?) {
                    //bleMessage.value = "指令发送失败"
                }
            })
    }

    fun copy() {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val text = originData.value.map { String.format("0x%02X", it) }.chunked(40).map {
            it.joinToString(",")
        }.joinToString(",\n") + "\n" + bleResponse.toString() + "\n"

        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text))
    }

    fun changeMtu(onMtuChanged: () -> Unit) {
        BleManager.getInstance().setMtu(bleDevice?.get(), 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                // 设置MTU失败
            }

            override fun onMtuChanged(mtu: Int) {
                BleManager.getInstance().setSplitWriteNum(min(mtu - 3,512))
                Log.e("BleViewModel", "设置mtu成功")
                onMtuChanged()
            }
        })
    }

    private fun statusLabel(status: Int): String {
        return when (status) {
            0 -> "命令正确"
            1 -> "命令码错误"
            2 -> "校验码错误"
            3 -> "数据长度错误"
            4 -> "数据无效"
            else -> "数据无效"
        };
    }

    private fun sportModeLabel(mode: Int): String {
        return when (mode) {
            0X00 -> "室内跑步"
            0X01 -> "户外跑步"
            0X03 -> "户外骑行"
            0X04 -> "健走"
            0X05 -> "跳绳"
            0X06 -> "足球"
            0X07 -> "羽毛球"
            0X09 -> "篮球"
            0X0A -> "椭圆机"
            0X0B -> "徒步"
            0X0C -> "瑜伽"
            0X0D -> "力量训练"
            0X0E -> "爬山"
            0X0F -> "自由运动"
            0X10 -> "户外步行"
            0X12 -> "室内单车"
            else -> "数据无效"
        };
    }

    class BluetoothListenerReceiver(val bleViewModel: BleViewModel) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        BluetoothAdapter.STATE_TURNING_ON -> Log.e(
                            "BleReceiver",
                            "onReceive---------蓝牙正在打开中"
                        )

                        BluetoothAdapter.STATE_ON -> {
                            Log.e("BleReceiver", "onReceive---------蓝牙已经打开")
                            Handler(Looper.getMainLooper()).postDelayed({
                                BleManager.getInstance().connect(bleViewModel.bleDevice?.get()?.mac,bleViewModel.bleGattCallback)
                            },1000)

                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            Log.e(
                                "BleReceiver",
                                "onReceive---------蓝牙正在关闭中"
                            )
                        }

                        BluetoothAdapter.STATE_OFF -> {
                            Log.e("BleReceiver", "onReceive---------蓝牙已经关闭")
                            bleViewModel.bleState = BleState.DISCONNECTED
                            BleManager.getInstance().destroy()
                        }
                    }
                }
            }
        }
    }

    private fun makeFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        return filter
    }

    fun bindDevice() : MutableMap<String,Any>{
        val bluetoothDevice = bleDevice!!.get()!!.device

        Log.e("BleViewModel", "绑定设备类型"+bluetoothDevice.type.toString() )
        val label = when(bluetoothDevice.type){
            1 -> "经典蓝牙"
            2 -> "LE蓝牙"
            3 -> "双模蓝牙"
            else -> "未知蓝牙"
        }
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
        var result = false
        if((bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_DUAL || bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            result = createBind(bluetoothDevice, BluetoothDevice.TRANSPORT_BREDR)
            Log.e("BleViewModel", "双模蓝牙绑定" + if(result) "成功" else "失败" )
        }else if(bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_LE){
            result = createBind(bluetoothDevice)
            Log.e("BleViewModel", "经典蓝牙绑定" + if(result) "成功" else "失败" )
        }

        val data: MutableMap<String, Any> = java.util.HashMap()
        data["is_success"] = result
        data["type"] = bluetoothDevice.type

        return data
    }
    fun createBind(device: BluetoothDevice?) : Boolean{
        var bRet = false
        if (Build.VERSION.SDK_INT >= 20) {
            bRet = device!!.createBond()
        } else {
            val btClass: Class<*> = device!!.javaClass
            try {
                val createBondMethod = btClass.getMethod("createBond")
                val `object` = createBondMethod.invoke(device) as? Boolean ?: return false
                bRet = `object`
            } catch (var6: java.lang.Exception) {
                var6.printStackTrace()
            }
        }

        return bRet
    }

    fun createBind(device: BluetoothDevice?,transport: Int) : Boolean{
        if(device == null) return false
        var bRet = false
        try{
            Log.e("BleViewModel", "进入双模蓝牙绑定" )
            val bluetoothDeviceClass = device.javaClass
            val createBondMethod = bluetoothDeviceClass.getDeclaredMethod("createBond",transport.javaClass)
            createBondMethod.isAccessible = true
            val obj = createBondMethod.invoke(device,transport)
            if(obj !is Boolean) return false
            bRet = obj
        }catch (e: Exception){
            e.printStackTrace()
        }
        return bRet
    }

    fun bindService(){
        context.bindService(
            Intent(
                context,
                RxBleService::class.java
            ), serviceConnection, Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService(){
        context.unbindService(serviceConnection)
    }
}