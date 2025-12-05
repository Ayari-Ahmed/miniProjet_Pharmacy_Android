package tn.rnu.isetr.miniprojet.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.168.240:5000/api/" // Your local machine IP address

    private lateinit var preferencesManager: PreferencesManager

    fun initialize(preferencesManager: PreferencesManager) {
        this.preferencesManager = preferencesManager
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = preferencesManager.getToken()

        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}