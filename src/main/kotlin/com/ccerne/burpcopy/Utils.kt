package com.ccerne.burpcopy

import burp.api.montoya.MontoyaApi

class Utils {
    companion object {
        private var debug = true
        lateinit var montoyaApi: MontoyaApi

        fun init (api: MontoyaApi) {
            this.montoyaApi = api
        }

        fun logDebug (text: String) {
            if (debug)
                montoyaApi.logging().logToOutput("[DEBUG] $text")
        }

        fun logInfo (text: String) {
            montoyaApi.logging().logToOutput("[INFO] $text")
        }

        fun logError(text: String) {
            montoyaApi.logging().logToError("[ERROR] $text")
        }

        fun encodeHtml(text: String): String = text.map { char ->
            when (char) {
                '<' -> "&lt;"
                '>' -> "&gt;"
                '&' -> "&amp;"
                '"' -> "&quot;"
                '\'' -> "&apos;"
                else -> char.toString()
            }
        }.joinToString("")

    }
}