package com.example.local_server


import android.content.Context
import android.util.Log
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

       fun sendToClient(message : String){
        mRequestHandler.apply {
            if (isClientConnected)
                messages.offer(message)
            else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun isClientConnected(): Boolean = mRequestHandler.isClientConnected

    fun setWebSocketCallback(callback : WebSocketCallback) = mRequestHandler.setCallback(callback)

    companion object {
        private const val TAG = "ClientServer"
    }

}