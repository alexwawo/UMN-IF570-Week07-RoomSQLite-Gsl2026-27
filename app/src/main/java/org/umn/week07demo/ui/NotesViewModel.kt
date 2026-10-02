package org.umn.week07demo.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.umn.week07demo.data.local.AppDatabase
import org.umn.week07demo.data.local.NoteEntity
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    // ViewModel tahu Room, tahu cara membuat database
    private val dao = AppDatabase.getInstance(application).noteDao()

    var notes by mutableStateOf<List<NoteEntity>>(emptyList())
        private set

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            notes = dao.getAll()
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            dao.insert(NoteEntity(title = title, content = content))
            loadNotes()      // refresh MANUAL, mudah lupa
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            dao.delete(note)
            loadNotes()      // refresh MANUAL lagi
        }
    }
}