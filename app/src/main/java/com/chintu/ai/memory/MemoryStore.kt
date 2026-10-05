package com.chintu.ai.memory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Memory(
    val id: Long,
    val category: String,
    val content: String,
    val createdAt: Long
)

class MemoryStore(
    context: Context
) : SQLiteOpenHelper(
    context,
    "chintu_memory.db",
    null,
    1
) {

    override fun onCreate(
        db: SQLiteDatabase
    ) {

        db.execSQL(
            """
            CREATE TABLE memory(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }

    fun save(
        category: String,
        content: String
    ): Long =

        writableDatabase.insert(
            "memory",
            null,
            android.content.ContentValues().apply {

                put(
                    "category",
                    category
                )

                put(
                    "content",
                    content
                )

                put(
                    "created_at",
                    System.currentTimeMillis()
                )
            }
        )

    fun list(
        query: String = ""
    ): List<Memory> {

        val out =
            mutableListOf<Memory>()

        val c =
            readableDatabase.rawQuery(
                """
                SELECT
                    id,
                    category,
                    content,
                    created_at
                FROM memory
                WHERE content LIKE ?
                ORDER BY created_at DESC
                """.trimIndent(),

                arrayOf(
                    "%$query%"
                )
            )

        c.use {

            while (
                it.moveToNext()
            ) {

                out += Memory(
                    it.getLong(0),
                    it.getString(1),
                    it.getString(2),
                    it.getLong(3)
                )
            }
        }

        return out
    }

    fun delete(
        id: Long
    ) =
        writableDatabase.delete(
            "memory",
            "id=?",
            arrayOf(
                id.toString()
            )
        ) > 0

    fun clear() {

        writableDatabase.delete(
            "memory",
            null,
            null
        )
    }

    fun update(
        id: Long,
        content: String
    ) {

        writableDatabase.execSQL(
            "UPDATE memory SET content=? WHERE id=?",
            arrayOf(
                content,
                id
            )
        )
    }
}
