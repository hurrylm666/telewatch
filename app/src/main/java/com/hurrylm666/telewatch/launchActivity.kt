package com.hurrylm666.telewatch

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Looper

class launchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)

        val sharedPref = getSharedPreferences("LoginPref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // 如果未登录，打开WelcomeActivity
            Handler(Looper.getMainLooper()).postDelayed({
                // 跳转逻辑
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }, 0) //启动画面显示的时间
        }else{
            // 如果已登录，打开MainActivity
            Handler(Looper.getMainLooper()).postDelayed({
                // 跳转逻辑
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 0) //启动画面显示的时间
        }
    }
}
