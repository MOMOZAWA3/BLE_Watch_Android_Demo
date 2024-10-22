package com.starmax.bluetoothsdk

import android.graphics.*
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starmax.net.data.Dial
import com.starmax.net.data.DialLabel
import com.starmax.net.repository.DialRepository
import com.starmax.net.response.AbstractResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StarmaxSendTest {
    @Test
    fun test() {
        DialRepository.getLists(
            "X01M01T001",
            { result: List<Dial?>?, response: AbstractResponse<*>? ->
                if (result != null) {
                    println(result)
                }
                println(result)
            },
            { e: Exception? ->
                Log.e("111", e!!.message!!)
            },
            true
        )

    }
}