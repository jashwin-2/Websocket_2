package com.zoho.vtouch.logging_agent

import android.content.Context
import android.util.Log
import com.zoho.vtouch.logging_agent.model.SessionDetails
import java.lang.NumberFormatException

object LoggingAgent {
    private const val DEFAULT_PORT = 8000
    private const val WEB_SOCKET_PORT = 8001
    private var clientServer: ClientServer? = null
    private var websocketsServer: WebSocketServer? = null
    private var addresses = mutableListOf<String>()


    fun initialize(
        context: Context?,
        serverPort: Int,
        data: SessionDetails,
        socketCallback: WebSocketCallback
    ): WebSocketServer {
        var portNumber: Int
        try {
            portNumber = serverPort
        } catch (ex: NumberFormatException) {
            Log.e("Logging Agnet", "PORT_NUMBER should be integer", ex)
            portNumber = DEFAULT_PORT
            Log.i("Loging Agent", "Using Default port : $DEFAULT_PORT")
        }
        clientServer = ClientServer(context, portNumber)
        clientServer!!.start(socketCallback)

        websocketsServer = WebSocketServer(context, WEB_SOCKET_PORT, data,socketCallback)
        websocketsServer!!.start()
        addresses = NetworkUtils.getAddress(serverPort)
        Log.d("Logging Agent", addresses.toString())
        return websocketsServer!!
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