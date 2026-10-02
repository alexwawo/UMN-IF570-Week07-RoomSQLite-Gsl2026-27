package org.umn.week07demo.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.umn.week07demo.NotesApplication
import org.umn.week07demo.ui.NotesViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            NotesViewModel(notesApplication().container.noteRepository)
        }
    }
}

fun CreationExtras.notesApplication(): NotesApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NotesApplication