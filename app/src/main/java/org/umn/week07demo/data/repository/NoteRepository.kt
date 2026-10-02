package org.umn.week07demo.data.repository

import kotlinx.coroutines.flow.Flow
import org.umn.week07demo.model.Note

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun addNote(title: String, content: String)
    suspend fun deleteNote(note: Note)
}