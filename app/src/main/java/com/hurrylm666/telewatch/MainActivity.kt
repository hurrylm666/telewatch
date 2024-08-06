package com.hurrylm666.telewatch

import GroupAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        println("开始初始化")

        val groupList = listOf("Item 1", "Item 2", "Item 3")

        with(binding.MessageGroups) {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = GroupAdapter(groupList)
        }

        // 初始化 TDLib 客户端
        client = Client.create({ update -> handleUpdate(update) }, null, null)
    }

    override fun onStart() {
        super.onStart()
        println("程序完成启动")
    }

    private fun handleUpdate(update: TdApi.Object) {
        when (update.constructor) {
            TdApi.UpdateNewMessage.CONSTRUCTOR -> {
                val newMsg = update as TdApi.UpdateNewMessage
                val msg = newMsg.message

                if (msg.content.constructor == TdApi.MessageText.CONSTRUCTOR) {
                    val msgText = msg.content as TdApi.MessageText
                    println("New message: ${msgText.text.text}")
                }
            }
            else -> {
                // ignore other type of updates
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在这里释放 TDLib 资源
        client.close()
    }
}
