package com.example.inventory

import android.app.Application
import com.example.inventory.data.ItemRoomDatabase


class InventoryApplication : Application() {
    // Using by lazy so the database is only created when needed
    // rather than when the application starts
    val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
}