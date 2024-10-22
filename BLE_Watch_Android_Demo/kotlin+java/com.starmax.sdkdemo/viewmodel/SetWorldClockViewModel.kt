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

class SetWorldClockViewModel: ViewModel(), KoinComponent {
    var selectedApps by mutableStateOf(listOf<Int>())

    val defaultApps = mapOf(
        1 to "北京",
        2 to "华盛顿",
        3 to "伦敦",
        4 to "巴黎",
        5 to "纽约",
        6 to "东京",
        7 to "上海",
        8 to "孟买",
        9 to "悉尼",
        10 to "洛杉矶",
        11 to "莫斯科",
        12 to "柏林",
        13 to "罗马",
        14 to "伊斯坦布尔",
        15 to "开罗",
        16 to "南京",
        17 to "温哥华",
        18 to "芝加哥",
        19 to "里约热内卢",
        20 to "阿姆斯特丹",
        21 to "新加坡",
        22 to "首尔",
        23 to "墨尔本",
        24 to "新德里",
        25 to "堪培拉",
        26 to "巴西利亚",
        27 to "墨西哥城",
        28 to "香港",
        29 to "斯德哥尔摩",
        30 to "巴塞罗那",
        31 to "慕尼黑"
    )

    val context : Context by inject()

    fun getFromBle(){
        StarmaxBleClient.instance.getWorldClocks().subscribe({
            selectedApps = it.citysList
            viewModelScope.launch {
                Toast.makeText(context, "获取世界时钟成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }

    fun sendToBle(){
        StarmaxBleClient.instance.setWorldClocks(selectedApps).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置世界时钟成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}