package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ram_listings")
data class RamListing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sellerName: String,
    val breed: String,
    val price: Double,
    val weightKg: Double,
    val location: String,
    val quantityAvailable: Int,
    val imageUrl: String,
    val imageColorHex: String,
    val earTagNumber: String = ""
)

@Entity(tableName = "escrow_transactions")
data class EscrowTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val ramBreed: String,
    val sellerName: String,
    val buyerName: String,
    val buyerPhone: String,
    val deliveryAddress: String,
    val quantity: Int,
    val totalAmount: Double,
    val escrowStatus: String, // "HELD_IN_ESCROW", "RELEASED", "REFUNDED", "UNDER_DISPUTE"
    val paymentCardLast4: String
)

@Entity(tableName = "escrow_disputes")
data class EscrowDispute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: Int,
    val ramBreed: String,
    val buyerName: String,
    val sellerName: String,
    val totalAmount: Double,
    val disputeReason: String, // "WEIGHT_DISCREPANCY", "HEALTH_ISSUES", "DELIVERY_DELAY"
    val description: String,
    val initiatedBy: String = "BUYER",
    val status: String // "OPEN", "RESOLVED_REFUNDED", "RESOLVED_RELEASED"
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String,
    val role: String, // "BUYER", "FARMER", "MEDIATOR" (or "" if not specified)
    val isLoggedIn: Boolean = false
)

