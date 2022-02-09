package com.example.local_server.model

data class SessionDetails(
    val tables: Map<Int , String> = mapOf(),
    val logs: Map<Int , String> = mapOf(),
    val graphs: Map<Int , String> = mapOf()
)