package com.zoho.vtouch.logging_agent


import android.content.Context
import android.util.Log
import com.zoho.vtouch.logging_agent.model.GraphData
import com.zoho.vtouch.logging_agent.model.SessionDetails
import com.zoho.vtouch.logging_agent.model.JsonData
import com.zoho.vtouch.logging_agent.model.LogMessage
import java.net.ServerSocket


class WebSocketServer(
    context: Context?,
    port: Int,
    sessionDetails: SessionDetails,
    private val socketCallback: WebSocketCallback
) : Runnable {
    private val mPort: Int = port
    private val mRequestHandler: WebSocketHandler =
        WebSocketHandler(context?.assets, sessionDetails, socketCallback)
    private var isRunning = false
    private var webSocket: ServerSocket? = null

    fun start() {
        isRunning = true
        Thread(this).start()
    }

    fun stop() {
        try {
            isRunning = false
            if (null != webSocket) {
                mRequestHandler.isClientConnected = false
                webSocket!!.close()
                webSocket = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing the server socket.", e)
        }
    }

    override fun run() {
        try {
            webSocket = ServerSocket(mPort)
            while (isRunning) {
                val socket = webSocket!!.accept()
                mRequestHandler.handle(socket)
                socket.close()

            }
        } catch (e: java.lang.Exception) {
            Log.d("Error", e.stackTraceToString())
            socketCallback.onError(e)
        }
    }

    fun sendStatsToClient(statistics: JsonData) {
        sendJsonToClient(statistics)
    }

    fun  sendJsonToClient(json : String){
        mRequestHandler.apply {
            if (isClientConnected) {
              messages.offer(json)
            } else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun sendJsonToClient(json: JsonData) {
        mRequestHandler.apply {
            if (isClientConnected) {
                messages.offer(json.toJSON())
            } else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun sendLogMessage(logMessage: LogMessage, id: String) {

        JsonData(JsonData.LOG_MESSAGE, logMessage, id).also {
            sendJsonToClient(it)
        }

    }

    fun sendGraphData(list: MutableList<GraphData>) {
        JsonData(JsonData.GRAPH_DATA, list).also {
            sendJsonToClient(it)
        }
    }

    fun isClientConnected(): Boolean = mRequestHandler.isClientConnected


    companion object {
        private const val TAG = "ClientServer"
    }


}