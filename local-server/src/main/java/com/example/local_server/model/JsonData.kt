package com.example.local_server.model

data class JsonData(val type:Int, val json : String)
{
    companion object{
        const val AUDIO_STATS = 1
        const val VIDEO_STATS = 2
        const val LOG_MESSAGE = 3
    }
}