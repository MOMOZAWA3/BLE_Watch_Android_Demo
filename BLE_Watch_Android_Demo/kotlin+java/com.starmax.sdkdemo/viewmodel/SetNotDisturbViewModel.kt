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
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SetNotDisturbViewModel : ViewModel(), KoinComponent {
    var onOff by mutableStateOf(false)

    var allDayOnOff by mutableStateOf(false)

    var startHour by mutableStateOf(0)

    var startMinute by mutableStateOf(0)

    var endHour by mutableStateOf(0)

    var endMinute by mutableStateOf(0)

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
        StarmaxBleClient.instance.getNotDisturb().subscribe({
            startHour = it.startHour
            startMinute = it.startMinute
            endHour = it.endHour
            endMinute = it.endMinute
            onOff = it.onOff
            allDayOnOff = it.allDayOnOff
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setNotDisturb(
            onOff, allDayOnOff, startHour, startMinute, endHour, endMinute
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置目标成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}