package com.example.local_server

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.local_server.Utils.detectMimeType
import java.io.*
import java.net.Socket
import java.util.Scanner;

class RequestHandler(private val mContext: Context) {
    private val mAssets: AssetManager = mContext.resources.assets

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun handle(socket: Socket) {
        var reader: BufferedReader? = null
        var output: PrintStream? = null
        try {
            var route: String? = null
            var inp = socket.getInputStream()
            val s = Scanner(inp, "UTF-8")
            val data: String = s.useDelimiter("\\r\\n\\r\\n").next()

            // Read HTTP headers and parse out the route.
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
           var line: String
           while (!TextUtils.isEmpty(data.also { line = it })) {
               if (line.startsWith("GET /")) {
                   val start = line.indexOf('/') + 1
                   val end = line.indexOf(' ', start)
                   route = line.substring(start, end)
                   break
               }
           }
           output = PrintStream(socket.getOutputStream())
           // Output stream that we send the response to
           if (route == null || route.isEmpty()) {
               route = "home.html"

           }
           val bytes: ByteArray
           if (route.startsWith("audio")) {
               bytes = Utils.loadContent("audio_stats.json", mAssets)!!

           } else if (route.startsWith("video")) {
               bytes = Utils.loadContent("video_stats.json", mAssets)!!
           }
           else if(route.startsWith("live")){
               Log.d("socket", line)

               WebSocketHandler().handle(socket,data,mAssets)
               Log.d("socket", route)
               return
           }
           else {
               bytes = Utils.loadContent(route, mAssets)!!
           }

           output.println("HTTP/1.0 200 OK")
           output.println("Content-Type: " + detectMimeType(route))
           output.println("Content-Length: " + bytes.size)
           output.println()
           output.write(bytes)
           output.flush()
       } finally {
           try {
               output?.close()
               reader?.close()
           } catch (e: Exception) {
               e.printStackTrace()
           }
       }


}
}


