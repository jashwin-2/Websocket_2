package com.example.local_server

import android.content.res.AssetManager
import android.text.TextUtils
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    fun detectMimeType(fileName: String): String? {
        return when {
            TextUtils.isEmpty(fileName) -> {
                null
            }
            fileName.endsWith(".html") -> {
                "text/html"
            }
            fileName.endsWith(".js") -> {
                "application/javascript"
            }
            fileName.endsWith(".css") -> {
                "text/css"
            }
            else -> {
                "application/octet-stream"
            }
        }
    }

    @Throws(IOException::class)
    fun loadContent(fileName: String?, assetManager: AssetManager): ByteArray? {
        var input: InputStream? = null
        return try {
            val output = ByteArrayOutputStream()
            input = assetManager.open(fileName!!)
            val buffer = ByteArray(1024)
            var size: Int
            while (-1 != input.read(buffer).also { size = it }) {
                output.write(buffer, 0, size)
            }
            output.flush()
            output.toByteArray()
        } catch (e: FileNotFoundException) {
            null
        } finally {
            try {
                input?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    @JvmStatic
    fun getJsonFromAssets(manager: AssetManager, fileName: String?): String? {
        return try {
            val inputStream: InputStream = manager.open(fileName!!)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, charset("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return sdf.format(Date())
    }

}