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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserInfoViewModel(

) : ViewModel() , KoinComponent{
    var sex by mutableStateOf(0)

    var age by mutableStateOf(0)

    var height by mutableStateOf(0)

    var weight by mutableStateOf(0)

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getUserInfo().subscribe({
            sex = it.sex
            age = it.age
            height = it.height
            weight = it.weight
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setUserInfo(
            sex,
                    age,
                    height,
                    weight,
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置用户信息成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}