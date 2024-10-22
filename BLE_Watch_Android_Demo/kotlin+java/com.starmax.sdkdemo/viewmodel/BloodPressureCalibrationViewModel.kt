package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify
import com.starmax.bluetoothsdk.Notify.HealthCalibration
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.Utils
import com.starmax.bluetoothsdk.data.CalibrationValue
import com.starmax.bluetoothsdk.data.NotifyType
import com.starmax.sdkdemo.utils.TestRepository
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.Date

class BloodPressureCalibrationViewModel : ViewModel(), KoinComponent {
    var type = 2
    var year by mutableStateOf(0)
    var month by mutableStateOf(0)
    var day by mutableStateOf(0)
    var value0 by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0))
    var value1 by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0))
    var value2 by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0))
    var value3 by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0))
    var value4 by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0))

    var label by mutableStateOf("未校准")
    val context : Context by inject()

    init {
        StarmaxBleClient.instance.healthCalibrationStatusStream()
            .subscribe(
                {
                    if(it.type == type){
                        label =  when(it.calibrationStatus){
                            0 -> "校准完成"
                            1 -> "校准中"
                            2 -> "校准失败"
                            3 -> "数据错误"
                            else -> "未知"
                        }
                    }
                },
                {

                }
            ).let {}
    }

    fun getFromBle(){
        val lastSendCalendar = Calendar.getInstance()
        StarmaxBleClient.instance.healthCalibration(
            type = type,
            cmd = 4,
            calendar = lastSendCalendar,
            value = listOf()
        ).subscribe({
            if(it is HealthCalibration){
                type = it.type
                year = it.year
                month = it.month
                day = it.day

                value0 = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 0,
                    data2 = 0
                )

                value1 = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 0,
                    data2 = 0
                )

                value2 = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 0,
                    data2 = 0
                )

                value3 = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 0,
                    data2 = 0
                )

                value4 = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 0,
                    data2 = 0
                )



                if(it.valueCount > 0){
                    it.valueList[0].let { todata ->
                        value0 = CalibrationValue(
                            hour = todata.hour,
                            minute = todata.minute,
                            data1 = todata.data1,
                            data2 = todata.data2
                        )
                    }
                }

                if(it.valueCount > 1){
                    it.valueList[1].let { todata ->
                        value1 = CalibrationValue(
                            hour = todata.hour,
                            minute = todata.minute,
                            data1 = todata.data1,
                            data2 = todata.data2
                        )
                    }
                }

                if(it.valueCount > 2){
                    it.valueList[2].let { todata ->
                        value2 = CalibrationValue(
                            hour = todata.hour,
                            minute = todata.minute,
                            data1 = todata.data1,
                            data2 = todata.data2
                        )
                    }
                }

                if(it.valueCount > 3){
                    it.valueList[3].let { todata ->
                        value3 = CalibrationValue(
                            hour = todata.hour,
                            minute = todata.minute,
                            data1 = todata.data1,
                            data2 = todata.data2
                        )
                    }
                }

                if(it.valueCount > 4){
                    it.valueList[4].let { todata ->
                        value4 = CalibrationValue(
                            hour = todata.hour,
                            minute = todata.minute,
                            data1 = todata.data1,
                            data2 = todata.data2
                        )
                    }
                }

                viewModelScope.launch {
                    Toast.makeText(context, "获取血压矫正成功", Toast.LENGTH_SHORT).show()
                }
            }
        },{

        }).let {

        }
    }

    fun sendToBle(){
        val calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DATE)

        StarmaxBleClient.instance.healthCalibration(
            type = type,
            cmd = 5,
            calendar = calendar,
            value = listOf(
                value0,
                value1,
                value2,
                value3,
                value4,
            )
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置血压矫正成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }

    fun clearCalibration(){
        val calendar = Calendar.getInstance()

        StarmaxBleClient.instance.healthCalibration(
            type = type,
            cmd = 6,
            calendar = calendar,
            value = listOf()
        ).subscribe({
            value0 = CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0)
            value1 = CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0)
            value2 = CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0)
            value3 = CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0)
            value4 = CalibrationValue(hour = 0,minute = 0,data1 = 0,data2 = 0)
            viewModelScope.launch {
                Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }



    }

    fun startCalibration(){
        val calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DATE)

        StarmaxBleClient.instance.healthCalibration(
            type = type,
            cmd = 7,
            calendar = calendar,
            value = listOf()
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "开始校准", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}