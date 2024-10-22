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

class GoalsViewModel(

) : ViewModel() , KoinComponent {
    var steps by mutableStateOf(1000)

    var heat by mutableStateOf(10000)

    var distance by mutableStateOf(10)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getGoals().subscribe({
            steps = it.steps
            heat = it.heat
            distance = it.distance
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setGoals(
            steps,
            heat,
            distance,
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置目标成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}