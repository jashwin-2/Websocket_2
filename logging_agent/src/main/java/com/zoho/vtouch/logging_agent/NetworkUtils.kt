package com.zoho.vtouch.logging_agent

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


object NetworkUtils {
    fun getAddressLog(context: Context, port: Int): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        @SuppressLint("DefaultLocale") val formattedIpAddress = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
        return "Open \nhttp://$formattedIpAddress:$port\nin your browser"
    }

    fun getAddress(port: Int): MutableList<String> {
        val list = mutableListOf<String>()

        try {
            val networkInterfaceEnumeration: Enumeration<NetworkInterface> =
                NetworkInterface.getNetworkInterfaces()
            while (networkInterfaceEnumeration.hasMoreElements()) {
                for (interfaceAddress in networkInterfaceEnumeration.nextElement()
                    .interfaceAddresses)
                    if (interfaceAddress.address.isSiteLocalAddress)
                        list.add( "http://${interfaceAddress.address.hostAddress}:$port\n")
            }
        } catch (e: SocketException) {
            e.printStackTrace()

        }
        return list
    }
}