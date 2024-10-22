package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.HealthCalibration
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.data.CalibrationValue
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class BloodSugarCalibrationViewModel() : ViewModel(), KoinComponent {
    var type = 1
    var year by mutableStateOf(0)
    var month by mutableStateOf(0)
    var day by mutableStateOf(0)
    var minValue by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 1,data2 = 0))
    var maxValue by mutableStateOf(CalibrationValue(hour = 0,minute = 0,data1 = 2,data2 = 0))
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
        ).subscribe({ it ->
            if(it is HealthCalibration){
                type = it.type
                year = it.year
                month = it.month
                day = it.day

                minValue = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 1,
                    data2 = 0
                )

                maxValue = CalibrationValue(
                    hour = 0,
                    minute = 0,
                    data1 = 2,
                    data2 = 0
                )

                it.valueList.firstOrNull {
                    it.data1 == 1
                }?.let { todata ->
                    minValue = CalibrationValue(
                        hour = todata.hour,
                        minute = todata.minute,
                        data1 = todata.data1,
                        data2 = todata.data2
                    )
                }

                it.valueList.firstOrNull {
                    it.data1 == 2
                }?.let { todata ->
                    maxValue = CalibrationValue(
                        hour = todata.hour,
                        minute = todata.minute,
                        data1 = todata.data1,
                        data2 = todata.data2
                    )
                }

                viewModelScope.launch {
                    Toast.makeText(context, "获取血糖矫正成功", Toast.LENGTH_SHORT).show()
                }
            }
        },{

        }).let {

        }
    }

    fun sendToBle(isMin: Boolean){
        val calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DATE)

        StarmaxBleClient.instance.healthCalibration(
            type = type,
            cmd = 5,
            calendar = calendar,
            value = listOf(
                (if(isMin) minValue else maxValue).also {
                    it.hour = calendar.get(Calendar.HOUR_OF_DAY)
                    it.minute = calendar.get(Calendar.MINUTE)
                }
            )
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置血糖矫正成功", Toast.LENGTH_SHORT).show()
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
            minValue = CalibrationValue(hour = 0,minute = 0,data1 = 1,data2 = 0)
            maxValue = CalibrationValue(hour = 0,minute = 0,data1 = 2,data2 = 0)
            viewModelScope.launch {
                Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }

    }

    fun startCalibration(){
        val calendar = Calendar.getInstance()

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