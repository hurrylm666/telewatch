package com.hurrylm666.telewatch

import android.os.Bundle
import android.os.Handler
import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Looper

class launchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)


        Handler(Looper.getMainLooper()).postDelayed({
            // 这里是跳转逻辑，例如跳转到MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 0) // 这里的2000ms就是启动画面显示的时间
    }
}
