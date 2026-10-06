package org.umn.week07demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.umn.week07demo.model.Note

@Composable
fun NotesScreen(viewModel: NotesViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    NotesContent(
        notes = notes,
        onAdd = viewModel::addNote,
        onDelete = viewModel::deleteNote,
        onTogglePin = viewModel::togglePin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    notes: List<Note>,                  // CHANGED: sebelumnya List<NoteEntity>
    onAdd: (String, String) -> Unit,
    onDelete: (Note) -> Unit,           // CHANGED: sebelumnya (NoteEntity) -> Unit
    onTogglePin: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Notes - Week 7") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onAdd(title, content)
                    title = ""
                    content = ""
                },
                enabled = title.isNotBlank()
            ) { Text("Add") }

            LazyColumn {
                items(notes, key = { it.id }) { note ->
                    ListItem(
//                        headlineContent = { Text(note.title) },
                        supportingContent = { Text(note.content) },
//                        trailingContent = {
//                            TextButton(onClick = { onDelete(note) }) { Text("Delete") }
//                        }
                        // trailingContent pada ListItem:
                        trailingContent = {
                            Row {
                                TextButton(onClick = { onTogglePin(note) }) {
                                    Text(if (note.isPinned) "Unpin" else "Pin")
                                }
                                TextButton(onClick = { onDelete(note) }) { Text("Delete") }
                            }
                        },
                        headlineContent = {
                            Text(if (note.isPinned) "📌 ${note.title}" else note.title)
                        },

                    )
                }
            }
        }
    }
}