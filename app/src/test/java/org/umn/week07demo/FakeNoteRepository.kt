package org.umn.week07demo

import org.umn.week07demo.data.repository.NoteRepository
import org.umn.week07demo.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeNoteRepository : NoteRepository {

    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1

    override fun observeNotes(): Flow<List<Note>> = notes

    override suspend fun addNote(title: String, content: String) {
        val now = System.currentTimeMillis()
        notes.update { it + Note(nextId++, title, content, now, isPinned = false, updatedAt = now) }
    }

    override suspend fun deleteNote(note: Note) {
        notes.update { list -> list.filterNot { it.id == note.id } }
    }

    override suspend fun togglePin(note: Note) {
        notes.update { list ->
            list.map { if (it.id == note.id) it.copy(isPinned = !it.isPinned) else it }
        }
    }
}