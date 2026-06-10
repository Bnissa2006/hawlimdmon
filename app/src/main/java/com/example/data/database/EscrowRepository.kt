package com.example.data.database

import kotlinx.coroutines.flow.Flow

class EscrowRepository(private val escrowDao: EscrowDao) {
    val allListings: Flow<List<RamListing>> = escrowDao.getAllListings()
    val allTransactions: Flow<List<EscrowTransaction>> = escrowDao.getAllTransactions()
    val allDisputes: Flow<List<EscrowDispute>> = escrowDao.getAllDisputes()

    suspend fun getListingsStatic(): List<RamListing> {
        return escrowDao.getListingsStatic()
    }

    suspend fun insertListing(listing: RamListing): Long {
        return escrowDao.insertListing(listing)
    }

    suspend fun insertListings(listings: List<RamListing>) {
        escrowDao.insertListings(listings)
    }

    suspend fun deleteListing(listing: RamListing) {
        escrowDao.deleteListing(listing)
    }

    suspend fun updateListing(listing: RamListing) {
        escrowDao.updateListing(listing)
    }

    suspend fun insertTransaction(transaction: EscrowTransaction): Long {
        return escrowDao.insertTransaction(transaction)
    }

    suspend fun updateTransactionStatus(id: Int, status: String) {
        escrowDao.updateTransactionStatus(id, status)
    }

    suspend fun getTransactionById(id: Int): EscrowTransaction? {
        return escrowDao.getTransactionById(id)
    }

    suspend fun insertDispute(dispute: EscrowDispute): Long {
        return escrowDao.insertDispute(dispute)
    }

    suspend fun updateDisputeStatus(id: Int, status: String) {
        escrowDao.updateDisputeStatus(id, status)
    }

    // User Operations
    val currentLoggedInUser: Flow<User?> = escrowDao.getLoggedInUserFlow()

    suspend fun getLoggedInUser(): User? = escrowDao.getLoggedInUser()

    suspend fun getUserByUsername(username: String): User? = escrowDao.getUserByUsername(username)

    suspend fun insertUser(user: User) {
        escrowDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        escrowDao.updateUser(user)
    }

    suspend fun logoutAllUsers() {
        escrowDao.logoutAllUsers()
    }
}
