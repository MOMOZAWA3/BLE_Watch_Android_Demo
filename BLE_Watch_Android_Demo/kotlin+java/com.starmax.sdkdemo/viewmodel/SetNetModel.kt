package com.starmax.sdkdemo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.starmax.net.NetApi
import com.starmax.net.NetChannel
import com.starmax.net.NetConfig

class SetNetModel : ViewModel() {
    var channel by mutableStateOf(NetChannel.Release)
    var server by mutableStateOf(NetApi.Server)

    fun getData(){
        channel = NetConfig.netChannel
        server = NetConfig.netApi
    }

    fun setServerData(api: NetApi){
        server = api
        NetConfig.netApi = api
    }

    fun setChannelData(newChannel: NetChannel){
        channel = newChannel
        NetConfig.netChannel = newChannel
    }
}