package org.umn.week07demo

import android.app.Application
import org.umn.week07demo.di.AppContainer
import org.umn.week07demo.di.DefaultAppContainer

class NotesApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}