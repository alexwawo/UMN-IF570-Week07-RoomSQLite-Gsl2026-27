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

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).noteDao()

    val notes: StateFlow<List<NoteEntity>> = dao.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            dao.insert(NoteEntity(title = title, content = content))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            dao.delete(note)
        }
    }
}