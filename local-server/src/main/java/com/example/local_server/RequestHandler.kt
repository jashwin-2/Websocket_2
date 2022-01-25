package com.example.local_server

import android.content.Context
import android.content.res.AssetManager
import android.text.TextUtils
import com.example.local_server.Utils.detectMimeType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.Socket
import java.util.*

class RequestHandler(mContext: Context) {
    private val mAssets: AssetManager = mContext.resources.assets

    @Throws(IOException::class)
    fun handle(socket: Socket) {
        var reader: BufferedReader? = null
        var output: PrintStream? = null
        try {

            var route: String? = null
            val inp = socket.getInputStream()
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
            val bytes: ByteArray = when {
                route.startsWith("audio") -> {
                    Utils.loadContent("audio_stats.json", mAssets)!!

                }
                route.startsWith("video") -> {
                    Utils.loadContent("video_stats.json", mAssets)!!
                }
                else -> {
                    Utils.loadContent(route, mAssets)!!
                }
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


