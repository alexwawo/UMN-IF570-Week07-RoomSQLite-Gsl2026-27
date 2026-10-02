package org.umn.week07demo.di

import android.content.Context
import org.umn.week07demo.data.repository.NoteRepository
import org.umn.week07demo.data.repository.NoteRepositoryImpl
import org.umn.week07demo.data.local.AppDatabase

interface AppContainer {
    val noteRepository: NoteRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    override val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(database.noteDao())
    }
}