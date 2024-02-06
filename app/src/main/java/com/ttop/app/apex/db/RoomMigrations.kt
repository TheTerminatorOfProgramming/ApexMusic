package com.ttop.app.apex.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE LyricsEntity")
        db.execSQL("DROP TABLE BlackListStoreEntity")
    }
}