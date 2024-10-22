package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HealthOpenViewModel(

) : ViewModel(),KoinComponent {
    var heartRate by mutableStateOf(false)

    var bloodPressure by mutableStateOf(false)

    var bloodOxygen by mutableStateOf(false)

    var pressure by mutableStateOf(false)

    var temp by mutableStateOf(false)

    var bloodSugar by mutableStateOf(false)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getHealthOpen().subscribe({
            heartRate = it.heartRate
            bloodPressure = it.bloodPressure
            bloodOxygen = it.bloodOxygen
            pressure = it.pressure
            temp = it.temp
            bloodSugar = it.bloodSugar
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setHealthOpen(
            heartRate,
            bloodPressure,
            bloodOxygen,
            pressure,
            temp,
            bloodSugar
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置开关成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}