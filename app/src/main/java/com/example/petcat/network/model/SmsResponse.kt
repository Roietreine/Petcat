package com.example.petcat.network.model

data class SmsResponse(
    val code: Int?,
    val `data`: List<Any?>?,
    val msg: String?,
    val time: String?
)