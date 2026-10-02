package org.umn.week07demo.model

import org.umn.week07demo.data.local.NoteEntity

fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt
)

fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt
)