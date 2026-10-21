package com.example.smoothnotes

import android.content.Context
import java.io.File

data class Note(val id: String, val title: String, val content: String, val modified: Long)

class NoteRepository(context: Context) {
    private val dir = File(context.filesDir, "notes").apply { mkdirs() }

    fun loadAll(): List<Note> =
        dir.listFiles { f -> f.extension == "txt" }
            ?.map { read(it) }
            ?.sortedByDescending { it.modified }
            ?: emptyList()

    fun save(id: String?, title: String, content: String): Note {
        val noteId = id ?: System.currentTimeMillis().toString()
        val file = File(dir, "$noteId.txt")
        file.writeText(title.replace('\n', ' ') + "\n" + content)
        return read(file)
    }

    fun delete(id: String) {
        File(dir, "$id.txt").delete()
    }

    private fun read(file: File): Note {
        val text = file.readText()
        val title = text.substringBefore('\n')
        val content = if ('\n' in text) text.substringAfter('\n') else ""
        return Note(file.nameWithoutExtension, title, content, file.lastModified())
    }
}
