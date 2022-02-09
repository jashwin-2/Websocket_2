package com.example.socket

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.local_server.LoggingAgent
import com.example.local_server.Utils.getJsonFromAssets
import com.example.local_server.WebSocketCallback
import com.example.local_server.WebSocketServer
import com.example.local_server.model.SessionDetails
import com.example.local_server.model.JsonData
import com.example.local_server.model.LogMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    var count = 1
    var webSocket: WebSocketServer? = null

    companion object {
        const val AUDIO_STATS = 1
        const val VIDEO_STATS = 2
        const val STATS_1 = 3
        const val STATS_2 = 4
        const val LOGS = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LoggingAgent.setOnErrorListener {
            Log.d("Error", it.stackTraceToString())
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
        val sessionDetails = SessionDetails(
            mapOf(
                AUDIO_STATS to "Audio Stats",
                VIDEO_STATS to "Video Stats", STATS_1 to "Stats 4", STATS_2 to "Stats 5"
            ), mapOf(LOGS to "Logs",7 to "Logs 1")
        )
        LoggingAgent.initialize(
            context = applicationContext,
            serverPort = 8000,
            sessionDetails
        ) { webSocket ->
            Log.d("Success", "Socket initialized")

            this.webSocket = webSocket
        }


        et_address.text = LoggingAgent.getAddress()

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
                    sendStats()
                    sendLog()
                }

            }
        }

    }

    override fun onDestroy() {
        LoggingAgent.shutDown()
        super.onDestroy()
    }

    fun sendStats() {
        val type = JsonData.TABLE_DATA
        val audioStats = Objects.requireNonNull(getJsonFromAssets(this.assets, "audio_stats.json"))
            .run { Gson().fromJson(this, Any::class.java) }
        val videoStats = Objects.requireNonNull(getJsonFromAssets(this.assets, "video_stats.json"))
            .run { Gson().fromJson(this, Any::class.java) }

        webSocket?.sendStatsToClient(JsonData(type, VIDEO_STATS, videoStats))
        webSocket?.sendStatsToClient(JsonData(type, STATS_1, videoStats))
        webSocket?.sendStatsToClient(JsonData(type, STATS_2, audioStats))
        webSocket?.sendStatsToClient(JsonData(type, AUDIO_STATS, audioStats))

    }

    fun sendLog() {
        val i = if (count%2==0) 6 else 7

        LogMessage(LogMessage.ERROR, "Error Log").also {
            webSocket?.sendLogMessage(it,i)
        }
        LogMessage(LogMessage.INFO, "Info log ").also {
            webSocket?.sendLogMessage(it,i)
        }
        LogMessage(LogMessage.WARN, "Warn Log").also {
            webSocket?.sendLogMessage(it,i)
        }
        count++

    }
}