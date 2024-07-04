package com.hurrylm666.telewatch

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.hurrylm666.telewatch.databinding.SettingActivityBinding
import java.io.File

class SettingActivity : ComponentActivity() {
    private lateinit var binding: SettingActivityBinding
    private lateinit var reset_libtd: String
    private lateinit var reset_self: String
    private lateinit var Clearing_cache: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置全屏或沉浸式体验（如果这是你的方法的作用）
        enableEdgeToEdge()

        // 设置应用的主题
        setTheme(android.R.style.Theme_DeviceDefault)

        // 使用正确的绑定类名，这个名字基于您的布局文件名
        binding = SettingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root) // 只调用一次 setContentView

        Clearing_cache = getString(com.hurrylm666.telewatch.R.string.Clearing_cache)
        reset_libtd = getString(com.hurrylm666.telewatch.R.string.Reset_libtd)
        reset_self = getString(com.hurrylm666.telewatch.R.string.Reset_self)

        // 先定义一个数据列表
        val dataList = arrayOf(Clearing_cache, reset_libtd, reset_self)

        // 初始化一个适配器
        val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, dataList)

        // 给ListView设置适配器
        binding.listview.adapter = adapter

        // ListView 设置点击事件监听器
        binding.listview.setOnItemClickListener { parent, view, position, id ->
            val item = adapter.getItem(position)
            when (item) {
                Clearing_cache -> {
                    cacheDir.deleteRecursively()
                    Toast.makeText(this, getString(com.hurrylm666.telewatch.R.string.Successful), Toast.LENGTH_SHORT).show()
                }
                reset_libtd -> reset_libtd()
                reset_self -> reset_self()
                else -> Toast.makeText(this, "What did you order?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reset_libtd(){
        val dir = File(applicationContext.filesDir.absolutePath)
        dir.listFiles()?.find { it.name == "tdlib" && it.isDirectory }?.deleteRecursively()
        // 清除登录数据
        getSharedPreferences("LoginPref", Context.MODE_PRIVATE).edit().clear().apply()
        // Toast提醒
        Toast.makeText(this, getString(com.hurrylm666.telewatch.R.string.Successful), Toast.LENGTH_SHORT).show()
        // 重启软件
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
        }, 1000)
    }

    private fun reset_self(){
        // 清除缓存
        cacheDir.deleteRecursively()
        // 清空软件文件
        filesDir.deleteRecursively()
        // 清空 SharedPreferences
        getSharedPreferences("LoginPref", Context.MODE_PRIVATE).edit().clear().apply()
        // Toast提醒
        Toast.makeText(this, getString(com.hurrylm666.telewatch.R.string.Successful), Toast.LENGTH_SHORT).show()
        // 重启软件
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
        }, 1000)
    }
}
