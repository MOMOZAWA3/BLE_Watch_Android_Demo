package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.timepicker.MaterialTimePicker
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HeartRateViewModel : ViewModel() , KoinComponent{
    var startHour by mutableStateOf(0)

    var startMinute by mutableStateOf(0)

    var endHour by mutableStateOf(0)

    var endMinute by mutableStateOf(0)

    var period by mutableStateOf(10)

    var alarmThreshold by mutableStateOf(1000)

    val context : Context by inject()

    fun setStartTime(picker: MaterialTimePicker){
        startHour = picker.hour
        startMinute = picker.minute
    }

    fun setEndTime(picker: MaterialTimePicker){
        endHour = picker.hour
        endMinute = picker.minute
    }

    fun getData() {
        StarmaxBleClient.instance.getHeartRateControl().subscribe({
            startHour = it.startHour
            startMinute = it.startMinute
            endHour = it.endHour
            endMinute = it.endMinute
            period = it.period
            alarmThreshold = it.alarmThreshold
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setHeartRateControl(
            startHour,
            startMinute,
            endHour,
            endMinute,
            period,
            alarmThreshold
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置心率成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}