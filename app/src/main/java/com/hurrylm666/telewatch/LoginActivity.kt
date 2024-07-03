package com.hurrylm666.telewatch

//tdlib
import android.content.Context
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.util.Properties
import com.hurrylm666.telewatch.databinding.LoginScreenBinding


class LoginActivity : ComponentActivity() {
    private lateinit var client: Client
    private lateinit var binding: LoginScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(android.R.style.Theme_DeviceDefault)
        setContentView(R.layout.login_screen)

        println("start")

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
                binding.state.text = getString(R.string.Request_error)
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
            findViewById<ImageView>(R.id.imageViewQrCode).setImageBitmap(bmp)
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

    override fun onDestroy() {
        super.onDestroy()
        // 在这里释放 TDLib 资源
        client.close()
    }
}
