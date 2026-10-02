package org.umn.week07demo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.umn.week07demo.data.local.NoteDao
import org.umn.week07demo.data.local.NoteEntity
import org.umn.week07demo.model.Note
import org.umn.week07demo.model.toDomain
import org.umn.week07demo.model.toEntity

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addNote(title: String, content: String) {
        dao.insert(NoteEntity(title = title, content = content))
    }

    override suspend fun deleteNote(note: Note) {
        dao.delete(note.toEntity())
    }
}