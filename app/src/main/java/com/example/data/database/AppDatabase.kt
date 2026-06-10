package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RamListing::class, EscrowTransaction::class, EscrowDispute::class, User::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun escrowDao(): EscrowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "herdescrow_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(PrepopulateCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrepopulateCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            prepopulateAll()
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            prepopulateAll()
        }

        private fun prepopulateAll() {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).escrowDao()
                dao.insertListings(
                    listOf(
                        RamListing(
                            id = 1,
                            sellerName = "الكساب الحاج بوعزة (سطات - قلعة السراغنة)",
                            breed = "سلالة الصردي الأصيلة المباركة (Sardi Pure Breed)",
                            price = 4500.00,
                            weightKg = 82.5,
                            location = "سطات (Settat)",
                            quantityAvailable = 3,
                            imageUrl = "img_sheep_sardi",
                            imageColorHex = "#FAF9F6",
                            earTagNumber = "ONSSA-SRD-2026-6453"
                        ),
                        RamListing(
                            id = 2,
                            sellerName = "الكساب التهامي لتربية المواشي (خنيفرة)",
                            breed = "سلالة تمحضيت الأطلس الممتازة (Timahdite Breed)",
                            price = 3200.00,
                            weightKg = 74.0,
                            location = "خنيفرة (Khénifra)",
                            quantityAvailable = 2,
                            imageUrl = "img_sheep_timahdite",
                            imageColorHex = "#CF7B3C",
                            earTagNumber = "ONSSA-TMH-2026-9214"
                        ),
                        RamListing(
                            id = 3,
                            sellerName = "مربي هضاب الشرق لتربية الماشية (الجهة الشرقية)",
                            breed = "سلالة بني غيل - الدغمة الحرة (Beni Guil)",
                            price = 3800.00,
                            weightKg = 78.0,
                            location = "فكيك (Figuig)",
                            quantityAvailable = 4,
                            imageUrl = "img_sheep_beniguil",
                            imageColorHex = "#E6B272",
                            earTagNumber = "ONSSA-BNG-2026-1185"
                        ),
                        RamListing(
                            id = 4,
                            sellerName = "ضيعة واحة تافيلالت لتربية المواشي (الرشيدية)",
                            breed = "سلالة الدمان الصحراوية المباركة (D'man Prolific)",
                            price = 2600.00,
                            weightKg = 61.2,
                            location = "الرشيدية (Errachidia)",
                            quantityAvailable = 1,
                            imageUrl = "img_sheep_dman",
                            imageColorHex = "#0F6F47",
                            earTagNumber = "ONSSA-DMN-2026-7842"
                        ),
                        RamListing(
                            id = 5,
                            sellerName = "موال أبي الجعد للماشية الأصيلة (خريبكة)",
                            breed = "سلالة بوجعد الصفراء الأصيلة (Boujaâd Breed)",
                            price = 3000.00,
                            weightKg = 68.5,
                            location = "بوجعد (Boujaâd)",
                            quantityAvailable = 5,
                            imageUrl = "img_sheep_timahdite",
                            imageColorHex = "#1BB270",
                            earTagNumber = "ONSSA-BJD-2026-3021"
                        )
                    )
                )
            }
        }
    }
}
