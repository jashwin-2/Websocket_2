package com.example.local_server.model

data class JsonData(val type:Int,val json : Any,val id : Int=0)
{
    companion object{
        const val TABLE_DATA = 1
        const val LOG_MESSAGE = 2
        const val GRAPH_DATA = 3
        const val INITIAL_DATA = 4
    }
}