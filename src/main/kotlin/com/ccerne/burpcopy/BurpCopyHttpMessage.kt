package com.ccerne.burpcopy

data class BurpCopyHttpMessage(
    val method: String?,
    val headers: Map<String, String>,
    val path: String?,
    val body: String,
    val httpVersion: String,
    val statusCode: Short,
    val reasonPhrase: String?
)
