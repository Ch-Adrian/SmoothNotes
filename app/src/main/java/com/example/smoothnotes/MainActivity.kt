package com.example.smoothnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smoothnotes.ui.theme.SmoothNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = NoteRepository(applicationContext)
        setContent {
            SmoothNotesTheme {
                NotesApp(repository)
            }
        }
    }
}

@Composable
fun NotesApp(repository: NoteRepository) {
    var notes by remember { mutableStateOf(repository.loadAll()) }
    var editing by remember { mutableStateOf<Note?>(null) }
    var creating by remember { mutableStateOf(false) }

    if (creating || editing != null) {
        val close = { creating = false; editing = null }
        BackHandler(onBack = close)
        EditorScreen(
            note = editing,
            onSave = { title, content ->
                repository.save(editing?.id, title, content)
                notes = repository.loadAll()
                close()
            },
            onDelete = editing?.let { n ->
                {
                    repository.delete(n.id)
                    notes = repository.loadAll()
                    close()
                }
            },
            onCancel = close
        )
    } else {
        NoteListScreen(notes, onNew = { creating = true }, onOpen = { editing = it })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(notes: List<Note>, onNew: () -> Unit, onOpen: (Note) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("SmoothNotes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNew) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Text("No notes yet. Tap + to create one.", modifier = Modifier.padding(padding).padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(notes, key = { it.id }) { note ->
                    ListItem(
                        headlineContent = { Text(note.title.ifBlank { "Untitled" }, maxLines = 1) },
                        supportingContent = { Text(note.content, maxLines = 2) },
                        modifier = Modifier.clickable { onOpen(note) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    note: Note?,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New note" else "Edit note") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel") } },
                actions = {
                    if (onDelete != null) TextButton(onClick = onDelete) { Text("Delete") }
                    TextButton(
                        onClick = { onSave(title, content) },
                        enabled = title.isNotBlank() || content.isNotBlank()
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).weight(1f)
            )
        }
    }
}
