package com.zoho.vtouch.logging_agent

import android.content.Context
import java.lang.Exception
import java.net.ServerSocket


class ClientServer(context: Context?, port: Int) : Runnable {
    private val mPort: Int = port
    private var socketCallBack : WebSocketCallback? = null
    private val mRequestHandler: RequestHandler = RequestHandler(context!!)
    var isRunning = false
    private var mServerSocket: ServerSocket? = null

    fun start(onError: WebSocketCallback) {
        isRunning = true
        this.socketCallBack = onError
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
            socketCallBack?.onError(e)
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
            socketCallBack?.onError(e)

        }
    }

    companion object {
        private const val TAG = "ClientServer"
    }

}