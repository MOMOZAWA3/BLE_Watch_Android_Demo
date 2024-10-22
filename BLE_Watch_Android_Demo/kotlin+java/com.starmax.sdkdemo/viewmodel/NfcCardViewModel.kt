package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import com.starmax.bluetoothsdk.data.Contact
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NfcCardViewModel : ViewModel() , KoinComponent {
    var cardName by mutableStateOf("")
    var cardType by mutableStateOf(0)
    var isCopy by mutableStateOf(false)

    val context : Context by inject()

    fun setData() {
        if(isCopy){
            StarmaxBleClient.instance.copyNfcCard(cardType,cardName).subscribe({
                viewModelScope.launch {
                    Toast.makeText(context, "准备复制卡片", Toast.LENGTH_SHORT).show()
                }
            }, {

            }).let { }
        }else{
            StarmaxBleClient.instance.createNfcWhiteCard(cardType,cardName).subscribe({
                viewModelScope.launch {
                    Toast.makeText(context, "准备创建白卡", Toast.LENGTH_SHORT).show()
                }
            }, {

            }).let { }
        }
    }
}