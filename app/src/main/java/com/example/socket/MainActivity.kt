package com.example.socket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.local_server.Socket

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Socket.initialize(applicationContext,8000)
        val addresses = Socket.addresses

        if (addresses.isNotEmpty()){
            var string = "Server Addresses : \n"
            for ( i in addresses)
                string+=i
            et_address.text = string
        }
        else
            et_address.text = "Device not connected to a Private network Please turn on WIFI or Hotspot and launch the app"
    }
}