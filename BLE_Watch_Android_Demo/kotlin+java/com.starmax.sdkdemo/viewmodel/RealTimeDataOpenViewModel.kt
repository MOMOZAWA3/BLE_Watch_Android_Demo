package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.StarmaxMapResponse
import com.starmax.bluetoothsdk.data.NotifyType
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealTimeDataOpenViewModel(

) : ViewModel() , KoinComponent {
    var gsensor by mutableStateOf(false)

    var steps by mutableStateOf(false)

    var heartRate by mutableStateOf(false)

    var bloodPressure by mutableStateOf(false)

    var bloodOxygen by mutableStateOf(false)

    var temp by mutableStateOf(false)

    var bloodSugar by mutableStateOf(false)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getRealTimeOpen().subscribe({
            gsensor = it.gsensor
            steps = it.steps
            heartRate = it.heartRate
            bloodPressure = it.bloodPressure
            bloodOxygen = it.bloodOxygen
            temp = it.temp
            bloodSugar = it.bloodSugar
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setRealTimeOpen(
            gsensor,
            steps,
            heartRate,
            bloodPressure,
            bloodOxygen,
            temp,
            bloodSugar
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}