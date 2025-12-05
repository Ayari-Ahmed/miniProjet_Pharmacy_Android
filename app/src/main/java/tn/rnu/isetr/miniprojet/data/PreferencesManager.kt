package tn.rnu.isetr.miniprojet.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString("user_data", userJson).apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString("user_data", null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }

    fun clearUser() {
        prefs.edit().remove("user_data").apply()
    }

    fun logout() {
        clearToken()
        clearUser()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}