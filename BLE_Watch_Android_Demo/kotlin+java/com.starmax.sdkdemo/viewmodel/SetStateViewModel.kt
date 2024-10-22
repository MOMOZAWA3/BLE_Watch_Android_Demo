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

class SetStateViewModel(

) : ViewModel() , KoinComponent {
    var timeFormat by mutableStateOf(0)
    var unitFormat by mutableStateOf(0)
    var tempFormat by mutableStateOf(0)
    var language by mutableStateOf(0)
    var backlighting by mutableStateOf(5)
    var screen by mutableStateOf(0)
    var wristUp by mutableStateOf(false)

    val context : Context by inject()
    fun getState() {
        StarmaxBleClient.instance.getState().subscribe({
            timeFormat = it.timeFormat
            unitFormat = it.unitFormat
            tempFormat = it.tempFormat
            language = it.language
            backlighting = it.backlighting
            screen = it.screen
            wristUp = it.wristUp
        }, {

        }).let { }
    }

    fun setState() {
        StarmaxBleClient.instance.setState(
            timeFormat,
            unitFormat,
            tempFormat,
            language,
            backlighting,
            screen,
            wristUp,
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}