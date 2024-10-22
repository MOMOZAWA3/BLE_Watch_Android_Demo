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

class SetContactViewModel : ViewModel() , KoinComponent {
    var firstName by mutableStateOf("")
    var firstNumber by mutableStateOf("")
    var secondName by mutableStateOf("")
    var secondNumber by mutableStateOf("")

    val context : Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getContacts().subscribe({
            for ( i in 0 until it.contactsCount){
                val contact = it.getContacts(i)
                if(i == 0){
                    firstName = contact.name
                    firstNumber = contact.phone
                }else if (i == 1){
                    secondName = contact.name
                    secondNumber = contact.phone
                }
            }
        }, {

        }).let { }
    }

    fun setData() {
        StarmaxBleClient.instance.setContacts(
            arrayListOf(
                Contact(firstName,firstNumber),
                Contact(secondName,secondNumber),
            )
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置联系人成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }
}