package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.StarmaxSend
import com.starmax.bluetoothsdk.data.CallControlType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CallControlViewModel(

) : ViewModel(), KoinComponent {
    var callControlType by mutableStateOf(CallControlType.Answer)

    var callNumber by mutableStateOf("")
    var exitNumber by mutableStateOf("")

    val context : Context by inject()

    fun send(){
        val number = when (callControlType) {
            CallControlType.Incoming -> callNumber
            CallControlType.Exit -> exitNumber
            else -> ""
        }

        StarmaxBleClient.instance.phoneControl(callControlType,number,false).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "发送电话成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}