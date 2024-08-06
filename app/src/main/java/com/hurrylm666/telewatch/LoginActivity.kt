package com.hurrylm666.telewatch

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.hurrylm666.telewatch.databinding.InputPasswordBinding
import java.util.Properties
import com.hurrylm666.telewatch.databinding.LoginScreenBinding


class LoginActivity : ComponentActivity() {
    private lateinit var client: Client
    private lateinit var binding: LoginScreenBinding
    private lateinit var inputPasswordBindingbinding: InputPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(android.R.style.Theme_DeviceDefault)

        // 初始化 TDLib 客户端
        client = Client.create({ update -> handleUpdate(update) }, null, null)

        // 使用正确的绑定类名，这个名字基于您的布局文件名
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root) // 只调用一次 setContentView

    }

    // 加载配置文件
    private fun loadConfig(): Properties {
        val properties = Properties()
        val inputStream = applicationContext.assets.open("config.properties")
        inputStream.use { properties.load(it) }
        return properties
    }

    // 处理 TDLib 更新的函数
    private fun handleUpdate(update: TdApi.Object) {
        when (update.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                val authorizationState = (update as TdApi.UpdateAuthorizationState).authorizationState
                when (authorizationState.constructor) {
                    TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                        //获取API ID和API Hash
                        val config = loadConfig()
                        val api_Id = config.getProperty("api_id").toInt()
                        val api_Hash = config.getProperty("api_hash")
                        // 设置 TDLib 参数
                        val parameters = TdApi.TdlibParameters().apply {
                            databaseDirectory = applicationContext.filesDir.absolutePath + "/tdlib"
                            useMessageDatabase = true
                            useSecretChats = true
                            apiId = api_Id
                            apiHash = api_Hash
                            systemLanguageCode = "en"
                            deviceModel = "Desktop"
                            systemVersion = "Unknown"
                            applicationVersion = "1.0"
                            enableStorageOptimizer = true
                        }
                        client.send(TdApi.SetTdlibParameters(parameters), { })
                    }
                    TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                        // 提供加密密钥
                        client.send(TdApi.CheckDatabaseEncryptionKey(), { })
                    }
                    TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                        // 请求二维码认证
                        client.send(TdApi.RequestQrCodeAuthentication(LongArray(0)), { authRequestHandler(it) })
                    }
                    TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                        val link = (authorizationState as TdApi.AuthorizationStateWaitOtherDeviceConfirmation).link
                        // 展示二维码
                        displayQrCode(link)
                    }
                    TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                        // 登录成功
                        println("Login Successful")
                        // 发送广播通知 WelcomeActivity 销毁自己
                        LocalBroadcastManager.getInstance(this).sendBroadcast(
                            Intent("ACTION_DESTROY_WELCOME_ACTIVITY")
                        )
                        // 存储登录成功信息
                        val sharedPref = getSharedPreferences("LoginPref", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putBoolean("isLoggedIn", true)
                            apply()
                        }
                        runOnUiThread{
                            // 启动新的页面
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                    TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                        //当需要输入密码时执行
                        val passwordState = authorizationState as TdApi.AuthorizationStateWaitPassword
                        val passwordHint = passwordState.passwordHint

                        // 进入密码输入函数
                        InputPassword(passwordHint)
                    }
                    // 处理其他授权状态...
                }
            }
            // 处理其他更新...
        }
    }

    // 处理认证请求的函数
    private fun authRequestHandler(result: TdApi.Object) {
        when (result.constructor) {
            TdApi.Error.CONSTRUCTOR -> {
                val error = result as TdApi.Error
                println("${getString(R.string.Request_error)} : ${error.message}")
                when (error.message) {
                    "PASSWORD_HASH_INVALID" -> {
                        inputPasswordBindingbinding.password.setText("")
                        inputPasswordBindingbinding.Done.text = getString(R.string.Password_Error)
                    }
                    else -> runOnUiThread {
                        AlertDialog.Builder(this)
                            .setMessage("${getString(R.string.Request_error)}\ncode:${error.code}\n${error.message}")
                            .setPositiveButton(getString(R.string.OK)) { dialog, which ->}
                            .show()
                    }
                }
                //binding.state.text = getString(R.string.Request_error)
            }
            // 处理其他结果...
        }
    }

    private fun displayQrCode(qrCodeLink: String) {
        val writer = MultiFormatWriter()
        try {
            val sizeInDp = 200 // 二维码的大小，以dp为单位
            val sizeInPx = this.dpToPx(sizeInDp) // 使用扩展函数将dp转换为px
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 0 // 设置边距为0

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(qrCodeLink, BarcodeFormat.QR_CODE, sizeInPx, sizeInPx, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            // 假设你有一个 ImageView 叫做 imageViewQrCode 来显示二维码
            binding.imageViewQrCode.setImageBitmap(bmp)
            // 更新 TextView 的内容
            binding.state.text = ""
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    //dp值转换为像素值
    private fun Context.dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            this.resources.displayMetrics
        ).toInt()
    }

    // 密码输入函数
    private fun InputPassword(passwordHint: String){
        // 获取input_password布局的binding
        inputPasswordBindingbinding = InputPasswordBinding.inflate(layoutInflater)
        //进入主线程（UI线程）执行修改主题代码
        runOnUiThread {
            // 切换页面
            binding.root.removeAllViews()
            binding.root.addView(inputPasswordBindingbinding.root)
        }

        // 添加密码提示到EditText
        inputPasswordBindingbinding.password.hint = passwordHint

        // 输入密码后执行
        inputPasswordBindingbinding.password.setOnEditorActionListener { v, actionId, event ->
            inputPasswordBindingbinding.Done.text = getString(R.string.Done)
            false
        }

        // 为StartMessaging按钮控件设置触摸监听器
        inputPasswordBindingbinding.Done.setOnTouchListener { v, event ->
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
                                // 手指仍在按钮上，继续登录
                                client.send(TdApi.CheckAuthenticationPassword(inputPasswordBindingbinding.password.text.toString()), { authRequestHandler(it) })
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
        super.onDestroy()
        // 在这里释放 TDLib 资源
        client.close()
    }
}
