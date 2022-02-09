package com.example.local_server


import android.content.Context
import android.util.Log
import com.example.local_server.model.SessionDetails
import com.example.local_server.model.JsonData
import com.example.local_server.model.LogMessage
import com.google.gson.Gson
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketException


class WebSocketServer(context: Context?, port: Int, sessionDetails : SessionDetails) : Runnable {
    private val mPort: Int = port
    private val mRequestHandler: WebSocketHandler = WebSocketHandler(context?.assets,sessionDetails)
    var isRunning = false
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
                mRequestHandler.handle(socket)
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
           sendJsonToClient(statistics)
    }

    private fun sendJsonToClient(statistics: JsonData) {
        mRequestHandler.apply {
            if (isClientConnected) {
                val data = Gson().toJson(statistics)
                messages.offer(data)
            } else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun sendLogMessage(logMessage : LogMessage,order : Int) {

            JsonData(JsonData.LOG_MESSAGE,order,logMessage).also {
                sendJsonToClient(it)
            }

    }

    fun isClientConnected(): Boolean = mRequestHandler.isClientConnected

    fun setWebSocketCallback(callback : WebSocketCallback) = mRequestHandler.setCallback(callback)

    companion object {
        private const val TAG = "ClientServer"
    }


}