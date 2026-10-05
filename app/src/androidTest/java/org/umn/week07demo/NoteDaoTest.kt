package org.umn.week07demo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.umn.week07demo.data.local.AppDatabase
import org.umn.week07demo.data.local.NoteDao
import org.umn.week07demo.data.local.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.noteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_thenObserve_returnsInsertedNote() = runTest {
        dao.insert(NoteEntity(title = "Title", content = "Body"))

        val notes = dao.observeAll().first()

        assertEquals(1, notes.size)
        assertEquals("Title", notes[0].title)
    }

    @Test
    fun delete_removesNote() = runTest {
        dao.insert(NoteEntity(title = "A", content = "B"))
        val inserted = dao.observeAll().first().single()

        dao.delete(inserted)

        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun pinnedNote_isOrderedFirst() = runTest {
        dao.insert(NoteEntity(title = "old", content = "", createdAt = 1L))
        dao.insert(NoteEntity(title = "new", content = "", createdAt = 2L))
        val old = dao.observeAll().first().first { it.title == "old" }

        dao.setPinned(old.id, true)

        assertEquals(listOf("old", "new"), dao.observeAll().first().map { it.title })
    }
}