package com.example.local_server

import android.content.Context
import android.util.Log
import java.lang.NumberFormatException

object Socket {
    private val TAG = Socket::class.java.simpleName
    private const val DEFAULT_PORT = 8000
    private var clientServer: ClientServer? = null
    var addresses = mutableListOf<String>()
    fun initialize(context: Context?,port : Int) {
        var portNumber: Int
        try {
            portNumber = port
        } catch (ex: NumberFormatException) {
            Log.e(TAG, "PORT_NUMBER should be integer", ex)
            portNumber = DEFAULT_PORT
            Log.i(TAG, "Using Default port : $DEFAULT_PORT")
        }
        clientServer = ClientServer(context, portNumber)
        clientServer!!.start()
        addresses = NetworkUtils.getAddress(port)
        Log.d(TAG, addresses.toString())
    }


    fun shutDown() {
        if (clientServer != null) {
            clientServer!!.stop()
            clientServer = null
        }
    }

    val isServerRunning: Boolean
        get() = clientServer != null && clientServer!!.isRunning
}