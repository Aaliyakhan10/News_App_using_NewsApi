package com.example.newsapp.models

import java.io.Serializable
data class Source(
    val id: String? = "",  // Make sure default is not null
    val name: String? = ""  // Make sure default is not null
) : Serializable {
    override fun hashCode(): Int {
        return (id?.hashCode() ?: 0) + (name?.hashCode() ?: 0)
    }
}
