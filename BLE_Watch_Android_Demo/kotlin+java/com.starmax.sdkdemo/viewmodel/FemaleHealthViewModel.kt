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
import java.util.Calendar

class FemaleHealthViewModel : ViewModel(), KoinComponent {
    var numberOfDays by mutableStateOf(0)
    var cycleDays by mutableStateOf(0)
    var year by mutableStateOf(0)
    var month by mutableStateOf(0)
    var day by mutableStateOf(0)

    val context : Context by inject()

    fun getFromBle(){
        StarmaxBleClient.instance.getFemaleHealth().subscribe({
            numberOfDays = it.numberOfDays
            cycleDays = it.cycleDays
            year = it.year
            month = it.month
            day = it.day
            viewModelScope.launch {
                Toast.makeText(context, "获取女性健康成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }

    fun setDate(long: Long){
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = long

        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun sendToBle(){
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR,year)
        calendar.set(Calendar.MONTH,month - 1)
        calendar.set(Calendar.DAY_OF_MONTH,day)

        StarmaxBleClient.instance.setFemaleHealth(numberOfDays,cycleDays,calendar).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "获取女性健康成功", Toast.LENGTH_SHORT).show()
            }
        },{

        }).let {

        }
    }
}