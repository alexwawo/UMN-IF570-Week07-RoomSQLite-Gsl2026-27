package org.umn.week07demo

import org.umn.week07demo.ui.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeNoteRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        viewModel = NotesViewModel(repository)   // tanpa Application, tanpa Room
    }

    @Test
    fun initialState_isEmpty() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }

        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun addNote_updatesNotes() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }

        viewModel.addNote("Title", "Body")

        assertEquals(1, viewModel.notes.value.size)
        assertEquals("Title", viewModel.notes.value.first().title)
    }

    @Test
    fun deleteNote_removesFromNotes() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }
        viewModel.addNote("A", "B")

        viewModel.deleteNote(viewModel.notes.value.first())

        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun togglePin_flipsPinnedFlag() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }
        viewModel.addNote("A", "B")

        viewModel.togglePin(viewModel.notes.value.first())

        assertTrue(viewModel.notes.value.first().isPinned)
    }
}