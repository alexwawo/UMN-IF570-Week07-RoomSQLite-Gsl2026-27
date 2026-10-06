package org.umn.week07demo.model

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: Long,
    val isPinned: Boolean = false
)