package com.hurrylm666.telewatch

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.hurrylm666.telewatch.databinding.MainActivityBinding
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class MainActivity : ComponentActivity() {

    private lateinit var client: Client
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 TDLib 客户端
        client = Client.create({ update -> handleUpdate(update) }, null, null)

        // 发送获取用户信息的请求
        client.send(TdApi.GetMe(), { result ->
            if (result is TdApi.User){
                runOnUiThread{
                    binding.userName.text = "${result.firstName} ${result.lastName}"
                    binding.userId.text = result.id.toString()
                }
            }
        })

        // 发送获取联系人列表的请求
        client.send(TdApi.GetContacts(), { result ->
            if (result is TdApi.Users) {
                runOnUiThread {
                    binding.contactsCount.text = result.userIds.size.toString()
                }
            }
        })
    }

    private fun handleUpdate(update: TdApi.Object) {
        TODO("处理你的更新事件")
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在这里释放 TDLib 资源
        client.close()
    }
}
