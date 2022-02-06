package com.example.local_server

import android.content.Context
import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.net.ServerSocket
import java.net.SocketException


class ClientServer(context: Context?, port: Int) : Runnable {
    private val mPort: Int = port
    private var onError : ((Exception) -> Unit)? = null
    private val mRequestHandler: RequestHandler = RequestHandler(context!!)
    var isRunning = false
    private var mServerSocket: ServerSocket? = null

    fun start(onError: ((Exception) -> Unit)?) {
        isRunning = true
        this.onError = onError
        Thread(this).start()
    }

    fun stop() {
        try {
            isRunning = false
            if (null != mServerSocket) {
                mServerSocket!!.close()
                mServerSocket = null
            }
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    override fun run() {
        try {
            mServerSocket = ServerSocket(mPort)
           while (isRunning) {
               val socket = mServerSocket!!.accept()
               mRequestHandler.handle(socket)
               socket.close()
            }
        } catch (e: Exception) {
            onError?.invoke(e)

        }
    }

    companion object {
        private const val TAG = "ClientServer"
    }

}