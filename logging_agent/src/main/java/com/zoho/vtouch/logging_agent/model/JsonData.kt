package com.zoho.vtouch.logging_agent.model

import com.google.gson.Gson
import org.json.JSONObject

data class JsonData(val type:Int, val json: Any, val id: String="")
{
    val gson = Gson()
    companion object{
        const val TABLE_DATA = 1
        const val LOG_MESSAGE = 2
        const val GRAPH_DATA = 3
        const val INITIAL_DATA = 4
    }

    fun toJSON(): String {
        return if (json is JSONObject) {
            val obj = JSONObject()
            obj.put("type", type)
            obj.put("id", id)
            obj.put("json", json)
            obj.toString()
        } else {
            gson.toJson(this)
        }
    }
}