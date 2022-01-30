package com.example.local_server


import android.content.Context
import android.util.Log
import com.example.local_server.model.JsonData
import com.example.local_server.model.LogMessage
import com.google.gson.Gson
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketException


class WebSocketServer(context: Context?, port: Int) : Runnable {
    private val mPort: Int = port
    private val mRequestHandler: WebSocketHandler = WebSocketHandler(context?.assets)
    var isRunning = false
        private set
    private var weSocket: ServerSocket? = null

    fun start() {
        isRunning = true
        Thread(this).start()
    }

    fun stop() {
        try {
            isRunning = false
            if (null != weSocket) {
                mRequestHandler.isClientConnected = false
                weSocket!!.close()
                weSocket = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing the server socket.", e)
        }
    }

    override fun run() {
        try {
            weSocket = ServerSocket(mPort)
            while (isRunning) {

                val socket = weSocket!!.accept()
                mRequestHandler.handle(socket,weSocket)
                socket.close()

            }
        } catch (e: SocketException) {
            Log.d("Error",e.stackTraceToString())

        } catch (e: IOException) {
            Log.d("Error",e.stackTraceToString())
        } catch (ignore: Exception) {
            Log.d("Error",ignore.stackTraceToString())
        }
    }

       fun sendStatsToClient(statistics : JsonData){
           sendJsonStringToClient(statistics)
    }

    private fun sendJsonStringToClient(statistics: JsonData) {
        mRequestHandler.apply {
            if (isClientConnected) {
                val data = Gson().toJson(statistics)
                messages.offer(data)
            } else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun sendLogMessage(logMessage : LogMessage){
        val data = Gson().toJson(logMessage)
        JsonData(JsonData.LOG_MESSAGE,data).also {
            sendJsonStringToClient(it)

        }
    }

    fun isClientConnected(): Boolean = mRequestHandler.isClientConnected

    fun setWebSocketCallback(callback : WebSocketCallback) = mRequestHandler.setCallback(callback)

    companion object {
        private const val TAG = "ClientServer"
    }

}