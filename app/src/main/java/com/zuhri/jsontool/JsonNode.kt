package com.zuhri.jsontool

// Satu baris tampilan dalam daftar hasil parsing JSON/XML
data class JsonNode(
    val key: String,
    val value: String,
    val depth: Int
)
