package com.example.myfireflydigital.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myfireflydigital.data.local.dao.CitaDao
import com.example.myfireflydigital.data.local.entities.CitaEntity

@Database(
    entities = [CitaEntity::class],
    version = 1
    //exportSchema = true
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun citaDao(): CitaDao
}