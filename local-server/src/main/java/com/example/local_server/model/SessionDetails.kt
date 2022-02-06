package com.example.local_server.model

data class SessionDetails(
    val tables: List<Pair<Int, String>> = listOf(),
    val log: Pair<Int, String>? = null,
    val graphs: List<Pair<Int, String>> = listOf()
)