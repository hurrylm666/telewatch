package com.hurrylm666.telewatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hurrylm666.telewatch.databinding.WelcomeActivityBinding

class WelcomeActivity : ComponentActivity() {
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var destroyReceiver: BroadcastReceiver
    private lateinit var binding: WelcomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置全屏或沉浸式体验（如果这是你的方法的作用）
        enableEdgeToEdge()

        // 设置应用的主题
        setTheme(android.R.style.Theme_DeviceDefault)

        // 使用正确的绑定类名，这个名字基于您的布局文件名
        binding = WelcomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root) // 只调用一次 setContentView

        // 收到广播，销毁页面代码
        destroyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "ACTION_DESTROY_WELCOME_ACTIVITY") {
                    finish()
                }
            }
        }

        // 注册广播接收器，便于销毁页面
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val filter = IntentFilter("ACTION_DESTROY_WELCOME_ACTIVITY")
        localBroadcastManager.registerReceiver(destroyReceiver, filter)

        // 为Setting TextView控件设置触摸监听器
        binding.Setting.setOnClickListener{
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        // 为StartMessaging按钮控件设置触摸监听器
        binding.StartMessaging.setOnTouchListener { v, event ->
            // 获取按钮的边界
            val rect = Rect(v.left, v.top, v.right, v.bottom)

            // 处理不同的触摸事件
            when (event.action) {
                // 按钮按下事件
                MotionEvent.ACTION_DOWN -> {
                    // 缩小按钮的动画
                    v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(150)
                        .start()
                    true // 返回true表示事件已被处理
                }
                // 按钮抬起事件
                MotionEvent.ACTION_UP -> {
                    // 放大按钮的动画
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .withEndAction {
                            // 检查手指是否还在按钮上
                            if (rect.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                                // 手指仍在按钮上，启动LoginActivity
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        .start()
                    true // 返回true表示事件已被处理
                }
                // 按钮取消事件，例如手指移出屏幕
                MotionEvent.ACTION_CANCEL -> {
                    // 恢复按钮大小的动画
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                    true // 返回true表示事件已被处理
                }
                // 按钮移动事件
                MotionEvent.ACTION_MOVE -> {
                    // 检查手指是否移出了按钮的边界
                    if (!rect.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        // 手指移出了按钮，取消动画并恢复按钮大小
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                        v.scaleX = 1f
                        v.scaleY = 1f
                    }
                    true // 返回true表示事件已被处理
                }
                else -> false // 其他事件不处理，返回false
            }
        }
    }
    override fun onDestroy() {
        if(::localBroadcastManager.isInitialized){
            localBroadcastManager.unregisterReceiver(destroyReceiver)
        }
        super.onDestroy()
    }
}
