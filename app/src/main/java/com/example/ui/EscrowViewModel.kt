package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.EscrowDispute
import com.example.data.database.EscrowRepository
import com.example.data.database.EscrowTransaction
import com.example.data.database.RamListing
import com.example.data.database.User
import com.example.ui.i18n.Language
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EscrowViewModel(private val repository: EscrowRepository) : ViewModel() {

    // Language state (AR default)
    private val _currentLanguage = MutableStateFlow(Language.AR)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    // Active UI Persona (null = Onboarding / Welcome screen)
    private val _activeRole = MutableStateFlow<String?>(null) // "BUYER", "FARMER", "MEDIATOR"
    val activeRole: StateFlow<String?> = _activeRole.asStateFlow()

    val currentLoggedInUser: StateFlow<User?> = repository.currentLoggedInUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            val loggedInUser = repository.getLoggedInUser()
            if (loggedInUser != null && loggedInUser.role.isNotEmpty()) {
                _activeRole.value = loggedInUser.role
            }
            
            // Handle self-healing/prepopulation if listings are unexpectedly empty
            try {
                if (repository.getListingsStatic().isEmpty()) {
                    repository.insertListings(
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Filters & Queries for Listings
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRegion = MutableStateFlow("All")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    // Technical Spec popup control
    private val _showSpecDialog = MutableStateFlow(false)
    val showSpecDialog: StateFlow<Boolean> = _showSpecDialog.asStateFlow()

    // Vet certification process simulation state
    private val _isVetCapturing = MutableStateFlow(false)
    val isVetCapturing: StateFlow<Boolean> = _isVetCapturing.asStateFlow()

    private val _vetCaptureProgress = MutableStateFlow(0f)
    val vetCaptureProgress: StateFlow<Float> = _vetCaptureProgress.asStateFlow()

    private val _isVetCertified = MutableStateFlow(false)
    val isVetCertified: StateFlow<Boolean> = _isVetCertified.asStateFlow()

    // Database reactive streams
    val ramListings: StateFlow<List<RamListing>> = repository.allListings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val escrowTransactions: StateFlow<List<EscrowTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val escrowDisputes: StateFlow<List<EscrowDispute>> = repository.allDisputes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Actions
    fun switchLanguage(language: Language) {
        _currentLanguage.value = language
    }

    fun setActiveRole(role: String?) {
        _activeRole.value = role
    }

    fun registerNewUser(
        username: String,
        passwordRaw: String,
        fullName: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedName = username.trim()
            if (trimmedName.isEmpty() || passwordRaw.isEmpty()) {
                onError("Please fill in username and password.")
                return@launch
            }
            val existing = repository.getUserByUsername(trimmedName)
            if (existing != null) {
                onError("Username already exists.")
                return@launch
            }
            // Logout others first
            repository.logoutAllUsers()
            val newUser = User(
                username = trimmedName,
                passwordHash = passwordRaw,
                fullName = fullName,
                phone = phone,
                role = "", // trigger form selection
                isLoggedIn = true
            )
            repository.insertUser(newUser)
            _activeRole.value = null
            onSuccess()
        }
    }

    fun loginExistingUser(
        username: String,
        passwordRaw: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedName = username.trim()
            if (trimmedName.isEmpty() || passwordRaw.isEmpty()) {
                onError("Please fill in credentials.")
                return@launch
            }
            val existing = repository.getUserByUsername(trimmedName)
            if (existing == null || existing.passwordHash != passwordRaw) {
                onError("Invalid credentials.")
                return@launch
            }
            repository.logoutAllUsers()
            val updatedUser = existing.copy(isLoggedIn = true)
            repository.updateUser(updatedUser)
            _activeRole.value = if (updatedUser.role.isNotEmpty()) updatedUser.role else null
            onSuccess()
        }
    }

    fun saveUserRoleSelection(role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = repository.getLoggedInUser()
            if (user != null) {
                val updated = user.copy(role = role)
                repository.updateUser(updated)
                _activeRole.value = role
                onSuccess()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAllUsers()
            _activeRole.value = null
        }
    }


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedRegion(region: String) {
        _selectedRegion.value = region
    }

    fun setSpecDialogVisible(visible: Boolean) {
        _showSpecDialog.value = visible
    }

    // Simulate Veterinary Camera Capture
    fun startVetCameraSimulation(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isVetCapturing.value = true
            _isVetCertified.value = false
            _vetCaptureProgress.value = 0f
            
            // Loop progress simulation
            for (i in 1..10) {
                delay(200)
                _vetCaptureProgress.value = i / 10f
            }
            
            _isVetCapturing.value = false
            _isVetCertified.value = true
            onComplete()
        }
    }

    fun resetVetCertification() {
        _isVetCertified.value = false
        _vetCaptureProgress.value = 0f
    }

    fun forceVetCertifiedStatus(certified: Boolean) {
        _isVetCertified.value = certified
        if (certified) {
            _isVetCapturing.value = false
            _vetCaptureProgress.value = 1f
        } else {
            _vetCaptureProgress.value = 0f
        }
    }

    // Checkout / Place Fund in Escrow
    fun placeFundsInEscrow(
        listingId: Int,
        breed: String,
        seller: String,
        buyerName: String,
        buyerPhone: String,
        address: String,
        quantity: Int,
        totalPrice: Double,
        cardNumber16: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val last4 = if (cardNumber16.length >= 4) cardNumber16.takeLast(4) else "4321"
            
            val transaction = EscrowTransaction(
                listingId = listingId,
                ramBreed = breed,
                sellerName = seller,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                deliveryAddress = address,
                quantity = quantity,
                totalAmount = totalPrice,
                escrowStatus = "HELD_IN_ESCROW",
                paymentCardLast4 = last4
            )
            
            repository.insertTransaction(transaction)
            
            // Decrease quantity available or update listing in database if listing still exists
            val currentListings = ramListings.value
            val currentListing = currentListings.find { it.id == listingId }
            if (currentListing != null) {
                val newQty = (currentListing.quantityAvailable - quantity).coerceAtLeast(0)
                repository.updateListing(currentListing.copy(quantityAvailable = newQty))
            }
            
            onSuccess()
        }
    }

    // Release Funds to Farmer
    fun confirmDeliveryAndRelease(transactionId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Update the transaction status
            repository.updateTransactionStatus(transactionId, "RELEASED")
            
            // If there's an associated dispute, mark it resolved
            val activeDisputes = escrowDisputes.value
            val relatedDispute = activeDisputes.find { it.transactionId == transactionId && it.status == "OPEN" }
            if (relatedDispute != null) {
                repository.updateDisputeStatus(relatedDispute.id, "RESOLVED_RELEASED")
            }
            
            onSuccess()
        }
    }

    // Raise a Dispute (Buyer)
    fun raiseDispute(
        transactionId: Int,
        reason: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val transactions = escrowTransactions.value
            val tx = transactions.find { it.id == transactionId }
            if (tx != null) {
                // Update transaction status to under dispute
                repository.updateTransactionStatus(transactionId, "UNDER_DISPUTE")
                
                // Form a dispute entry
                val dispute = EscrowDispute(
                    transactionId = transactionId,
                    ramBreed = tx.ramBreed,
                    buyerName = tx.buyerName,
                    sellerName = tx.sellerName,
                    totalAmount = tx.totalAmount,
                    disputeReason = reason,
                    description = description,
                    status = "OPEN"
                )
                
                repository.insertDispute(dispute)
                onSuccess()
            }
        }
    }

    // Arbitrate Dispute (Mediator)
    fun arbitrateDispute(
        disputeId: Int,
        transactionId: Int,
        payoutToFarmer: Boolean, // True = Release to Farmer, False = Refund Buyer
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val targetDisputeStatus = if (payoutToFarmer) "RESOLVED_RELEASED" else "RESOLVED_REFUNDED"
            val targetTxStatus = if (payoutToFarmer) "RELEASED" else "REFUNDED"
            
            repository.updateDisputeStatus(disputeId, targetDisputeStatus)
            repository.updateTransactionStatus(transactionId, targetTxStatus)
            
            onSuccess()
        }
    }

    // Add Ram Listing (Farmer Tools)
    fun publishRamListing(
        breed: String,
        weight: Double,
        location: String,
        price: Double,
        seller: String,
        colorHex: String,
        earTagNumber: String,
        customImageUrl: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val lowerBreed = breed.lowercase()
            val finalImageUrl = customImageUrl ?: when {
                lowerBreed.contains("صردي") || lowerBreed.contains("sardi") -> 
                    "img_sheep_sardi"
                lowerBreed.contains("تمحضيت") || lowerBreed.contains("timahdite") -> 
                    "img_sheep_timahdite"
                lowerBreed.contains("بني") || lowerBreed.contains("guil") -> 
                    "img_sheep_beniguil"
                lowerBreed.contains("دمان") || lowerBreed.contains("d'man") -> 
                    "img_sheep_dman"
                else -> 
                    "img_sheep_sardi"
            }

            val newListing = RamListing(
                sellerName = seller,
                breed = breed,
                price = price,
                weightKg = weight,
                location = location,
                quantityAvailable = 3, // default stock
                imageUrl = finalImageUrl,
                imageColorHex = colorHex,
                earTagNumber = earTagNumber
            )
            repository.insertListing(newListing)
            resetVetCertification()
            onSuccess()
        }
    }
}

// ViewModel Factory boilerplate to construct with explicit repository
class EscrowViewModelFactory(private val repository: EscrowRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EscrowViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EscrowViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
