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

class PasswordViewModel: ViewModel(), KoinComponent {
    var password by mutableStateOf("")

    var isOpen by mutableStateOf(false)

    val context : Context by inject()

    fun getFromBle(){
        StarmaxBleClient.instance.getPassword().subscribe({
            password = it.password
            isOpen = it.isOpen
            viewModelScope.launch {
                Toast.makeText(context, "获取密码成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }

    fun sendToBle(){
        StarmaxBleClient.instance.setPassword(password,isOpen).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置密码成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}