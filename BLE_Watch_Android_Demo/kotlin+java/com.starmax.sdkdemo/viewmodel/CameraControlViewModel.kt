package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.data.CameraControlType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CameraControlViewModel(

) : ViewModel(), KoinComponent {
    var cameraControlType by mutableStateOf(CameraControlType.CameraIn)

    val context : Context by inject()

    fun send(){
        StarmaxBleClient.instance.cameraControl(cameraControlType).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置相机成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}