package com.example.socket

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.local_server.Socket
import com.example.local_server.Utils.getJsonFromAssets
import com.example.local_server.WebSocketCallback
import com.example.local_server.WebSocketServer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    var count = 1
    var webSocket: WebSocketServer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Socket.setOnErrorListener {
            Log.d("Error", it.stackTraceToString())
            Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
        }

        Socket.initialize(context = applicationContext, serverPort = 8000) { webSocket ->
            Log.d("Success", "Socket initialized")

            this.webSocket = webSocket
        }


        et_address.text = Socket.getAddress()

        webSocket?.setWebSocketCallback(object : WebSocketCallback {
            override fun onError(ex: Exception?) {
                ex?.localizedMessage?.let { Log.d("Error", it) }
            }

            override fun onMessageReceived(message: String?) {
                Log.d("Message Received", "Client : $message")
            }

        })

        var i = 0

        GlobalScope.launch {
            while (true) {
                delay(1000)
                if (webSocket?.isClientConnected() == true) {
                    getMessage()?.let {
                        webSocket?.sendToClient(it)
                    }
                }

            }
        }

    }

    override fun onDestroy() {
        Socket.shutDown()
        super.onDestroy()
    }

    fun getMessage(): String? {
//        count
//        val data = if (count % 2 == 0) "audio_stats.json" else "video_stats.json"
        val data = "audio_stats.json"
        return Objects.requireNonNull(getJsonFromAssets(this.assets, data))
    }
}