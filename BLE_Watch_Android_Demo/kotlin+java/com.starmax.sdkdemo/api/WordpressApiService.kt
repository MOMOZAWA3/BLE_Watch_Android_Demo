package com.starmax.sdkdemo.api

import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.Response // 确保这里是 retrofit2 的 Response

// 定义数据类
data class YourDataClass(val title: String, val content: String, val status: String)
data class YourResponseClass(val id: Int, val status: String)

// 定义 API 接口
interface WordpressApi {
    @Headers("Content-Type: application/json")
    @POST("posts")  // 这是 WordPress 的默认文章创建端点
    fun createPost(
        @Body data: YourDataClass,
        @Header("Authorization") authHeader: String
    ): Call<YourResponseClass>
}

class WordpressApiService {

    private val baseUrl = "https://24te533.edu2web.com/wp-json/wp/v2/"

    // 发送数据到 WordPress 的方法
    fun sendBluetoothDataToWordpress(title: String, content: String, status: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val wordpressApi = retrofit.create(WordpressApi::class.java)

        // 构造认证头：Basic base64_encode(username:password)
        val credentials = "24TE533 UPOD:sLmf 4ksQ cQxH Lmy2 CmUR zRw7"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        // 准备发送的数据
        val postData = YourDataClass(title, content, status)

        // 发起请求
        val call = wordpressApi.createPost(postData, authHeader)
        call.enqueue(object : Callback<YourResponseClass> {
            override fun onResponse(call: Call<YourResponseClass>, response: retrofit2.Response<YourResponseClass>) {
                if (response.isSuccessful) {
                    Log.d("WordpressApiService", "Post created successfully: ${response.body()?.id}")
                } else {
                    Log.e("WordpressApiService", "Failed to create post: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<YourResponseClass>, t: Throwable) {
                Log.e("WordpressApiService", "Error creating post: ${t.message}")
            }
        })
    }

    // 用于设置 OkHttpClient，如果需要额外的配置，比如拦截器，可以在这里处理
    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}
