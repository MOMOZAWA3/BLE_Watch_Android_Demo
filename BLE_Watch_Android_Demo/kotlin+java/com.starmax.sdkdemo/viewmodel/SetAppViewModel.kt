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

class SetAppViewModel: ViewModel(), KoinComponent {
    var selectedApps by mutableStateOf(listOf<Int>())

    val defaultApps = mapOf(
        1 to "血压",
        2 to "事件提醒",
    )

//    val defaultApps = mapOf(
//        1 to "呼吸训练",
//        2 to "梅脱",
//        3 to "语音助手",
//        4 to "计时器",
//        5 to "秒表",
//        6 to "计算器",
//        7 to "闹钟",
//        8 to "手电筒",
//        9 to "查找手机",
//        10 to "世界时钟",
//        11 to "番茄钟",
//        12 to "女性健康"
//        13 to "血糖研究"
//        14 to "血压研究"
//    )

    val context : Context by inject()

    fun getFromBle(){
        StarmaxBleClient.instance.getApps().subscribe({
            selectedApps = it.appsList
            viewModelScope.launch {
                Toast.makeText(context, "获取应用商店成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }

    fun sendToBle(){
        StarmaxBleClient.instance.setApps(selectedApps).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置应用商店成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}