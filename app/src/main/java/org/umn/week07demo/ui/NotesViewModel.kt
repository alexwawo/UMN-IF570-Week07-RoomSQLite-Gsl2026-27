package org.umn.week07demo.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.umn.week07demo.data.local.AppDatabase
import org.umn.week07demo.data.local.NoteEntity
import kotlinx.coroutines.launch
import org.umn.week07demo.data.repository.NoteRepository
import org.umn.week07demo.data.repository.NoteRepositoryImpl
import org.umn.week07demo.model.Note

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    // Masih membuat dependency sendiri (belum DI), tapi logika hanya bicara ke interface
    private val repository: NoteRepository =
        NoteRepositoryImpl(AppDatabase.getInstance(application).noteDao())

    val notes: StateFlow<List<Note>> = repository.observeNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addNote(title: String, content: String) {
        viewModelScope.launch { repository.addNote(title, content) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }
}