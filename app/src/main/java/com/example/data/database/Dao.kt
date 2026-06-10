package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EscrowDao {
    // Ram Listings
    @Query("SELECT * FROM ram_listings ORDER BY id DESC")
    fun getAllListings(): Flow<List<RamListing>>

    @Query("SELECT * FROM ram_listings")
    suspend fun getListingsStatic(): List<RamListing>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: RamListing): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<RamListing>)

    @Delete
    suspend fun deleteListing(listing: RamListing)

    @Update
    suspend fun updateListing(listing: RamListing)

    // Escrow Transactions
    @Query("SELECT * FROM escrow_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<EscrowTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: EscrowTransaction): Long

    @Query("UPDATE escrow_transactions SET escrowStatus = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: Int, status: String)

    @Query("SELECT * FROM escrow_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): EscrowTransaction?

    // Escrow Disputes
    @Query("SELECT * FROM escrow_disputes ORDER BY id DESC")
    fun getAllDisputes(): Flow<List<EscrowDispute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: EscrowDispute): Long

    @Query("UPDATE escrow_disputes SET status = :status WHERE id = :id")
    suspend fun updateDisputeStatus(id: Int, status: String)

    // Users Management
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
}
