package com.hurrylm666.telewatch

//tdlib
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi.*

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import java.util.Properties
import com.hurrylm666.telewatch.databinding.LoginScreenBinding

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(android.R.style.Theme_DeviceDefault)
        setContentView(R.layout.login_screen)

        val config = loadConfig()
        val apiId = config.getProperty("api_id").toInt()
        val apiHash = config.getProperty("api_hash")

        // 使用正确的绑定类名，这个名字基于您的布局文件名
        val binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root) // 只调用一次 setContentView

        // 更新 TextView 的内容
        binding.state.text = apiHash
    }

    // 加载配置文件
    private fun loadConfig(): Properties {
        val properties = Properties()
        val inputStream = applicationContext.assets.open("config.properties")
        inputStream.use { properties.load(it) }
        return properties
    }
}
