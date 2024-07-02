package com.hurrylm666.telewatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hurrylm666.telewatch.ui.theme.TelewatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setTheme(android.R.style.Theme_DeviceDefault)

        setContentView(R.layout.activity_main)

        //val imageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imageView_ico)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TelewatchTheme {
        Greeting("Android")
    }
}