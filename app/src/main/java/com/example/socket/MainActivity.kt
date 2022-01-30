package com.example.socket

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.local_server.Socket
import com.example.local_server.Utils.getJsonFromAssets
import com.example.local_server.WebSocketCallback
import com.example.local_server.WebSocketServer
import com.example.local_server.model.JsonData
import com.example.local_server.model.LogMessage
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
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
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
                        webSocket?.sendStatsToClient(it)
                    }
                    sendLog()
                }

            }
        }

    }

    override fun onDestroy() {
        Socket.shutDown()
        super.onDestroy()
    }

    fun getMessage(): JsonData? {
        count++
        val fileName: String
        val type: Int
        if (count % 2 == 0) {
            fileName = "audio_stats.json"
            type = JsonData.AUDIO_STATS

        } else {
            fileName = "video_stats.json"
            type = JsonData.VIDEO_STATS
        }
        val stats = Objects.requireNonNull(getJsonFromAssets(this.assets, fileName))
        return stats?.let { JsonData(type, it) }

    }

    fun sendLog() {
        LogMessage(LogMessage.ERROR, "Error Log").also {
            webSocket?.sendLogMessage(it)
        }
        LogMessage(LogMessage.INFO, "Info log ").also {
            webSocket?.sendLogMessage(it)
        }
        LogMessage(LogMessage.WARN, "Warn Log").also {
            webSocket?.sendLogMessage(it)
        }
    }
}