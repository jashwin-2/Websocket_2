package com.example.local_server

import android.content.Context
import android.util.Log
import java.lang.Exception
import java.lang.NumberFormatException

object LoggerAgent {
    private val TAG = LoggerAgent::class.java.simpleName
    private const val DEFAULT_PORT = 8000
    private const val WEB_SOCKET_PORT = 8001
    private var clientServer: ClientServer? = null
    private var websocketsServer : WebSocketServer? = null
    private var addresses = mutableListOf<String>()

      fun initialize(context: Context?,serverPort : Int , onWebSocketInitialized : (webSocket : WebSocketServer)->Unit) {
        var portNumber: Int
        try {
            portNumber = serverPort
        } catch (ex: NumberFormatException) {
            Log.e(TAG, "PORT_NUMBER should be integer", ex)
            portNumber = DEFAULT_PORT
            Log.i(TAG, "Using Default port : $DEFAULT_PORT")
        }
        clientServer = ClientServer(context, portNumber)
        clientServer!!.start(onError)

        websocketsServer = WebSocketServer(context, WEB_SOCKET_PORT)
        websocketsServer!!.start()
          onWebSocketInitialized(websocketsServer!!)
        addresses = NetworkUtils.getAddress(serverPort)
        Log.d(TAG, addresses.toString())
    }


    fun shutDown() {
        if (clientServer != null) {
            clientServer!!.stop()
            clientServer = null
        }
        if (websocketsServer != null) {
            websocketsServer!!.stop()
            websocketsServer = null
        }
    }

    val isServerRunning: Boolean
        get() = clientServer != null && clientServer!!.isRunning

    var onError : ((Exception) -> Unit)? = null

    fun setOnErrorListener(errorCallback : (exp :Exception)->Unit) {
        onError = errorCallback
    }

    fun getAddress(): String {
        return if (addresses.isNotEmpty()) {
            var string = "Server Addresses : \n"
            for (i in addresses)
                string += i
            string
        } else
            "Device not connected to a Private network Please turn on WIFI or Hotspot and launch the app"

    }

}