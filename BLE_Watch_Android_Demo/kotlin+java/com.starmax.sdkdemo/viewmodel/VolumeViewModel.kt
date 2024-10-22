package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VolumeViewModel(

) : ViewModel() , KoinComponent {
    var volume by mutableStateOf(0)
    var maxVolume by mutableStateOf(15)
    var streamType by mutableStateOf(AudioManager.STREAM_MUSIC)

    val context : Context by inject()

    fun getManager() : AudioManager{
        return (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
    }

    fun getVolume() {
        volume = getManager().getStreamVolume(streamType)
        maxVolume = getManager().getStreamMaxVolume(streamType)
    }

    fun setVolume(){
        val currentThread = Thread.currentThread()
        val threadName = currentThread.name

        Log.e("VolumeViewModel","当前线程名称：$threadName")

        getManager().setStreamVolume(streamType,volume,AudioManager.FLAG_SHOW_UI)

    }
}