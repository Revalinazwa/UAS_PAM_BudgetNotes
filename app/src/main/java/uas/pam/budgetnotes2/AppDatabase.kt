package uas.pam.budgetnotes2

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}