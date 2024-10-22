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

class RealTimeMeasureOpenViewModel() : ViewModel() , KoinComponent {
    var heartRate by mutableStateOf(false)

    val context : Context by inject()

    fun setData() {
        StarmaxBleClient.instance.openRealTimeMeasure(
            0x01,
            heartRate
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置状态成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}