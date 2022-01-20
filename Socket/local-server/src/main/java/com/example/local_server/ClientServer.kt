package com.example.local_server

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException
import java.lang.Exception
import java.net.ServerSocket
import java.net.SocketException


class ClientServer(context: Context?, port: Int) : Runnable {
    private val mPort: Int = port
    private val mRequestHandler: RequestHandler = RequestHandler(context!!)
    var isRunning = false
        private set
    private var mServerSocket: ServerSocket? = null

    fun start() {
        isRunning = true
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
            Log.e(TAG, "Error closing the server socket.", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run() {
        try {
            mServerSocket = ServerSocket(mPort)
           while (isRunning) {
               val socket = mServerSocket!!.accept()

               Log.d(TAG, "runing")
               mRequestHandler.handle(socket)
               socket.close()
            }
        } catch (e: SocketException) {
            Log.e(TAG, "Web server error.", e)

        } catch (e: IOException) {
            Log.e(TAG, "Web server error.", e)
        } catch (ignore: Exception) {
            Log.e(TAG, "Exception.", ignore)
        }
    }

    companion object {
        private const val TAG = "ClientServer"
    }

}