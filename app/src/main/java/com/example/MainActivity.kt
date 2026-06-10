package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.database.EscrowDispute
import com.example.data.database.EscrowRepository
import com.example.data.database.EscrowTransaction
import com.example.data.database.RamListing
import com.example.ui.EscrowViewModel
import com.example.ui.EscrowViewModelFactory
import com.example.ui.i18n.Language
import com.example.ui.i18n.Translations
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            // Database and Repository initialization (scoped to MainActivity runtime)
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { EscrowRepository(database.escrowDao()) }
            val viewModel: EscrowViewModel = viewModel(
                factory = EscrowViewModelFactory(repository)
            )

            val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
            val themeMode = isSystemInDarkTheme()

            // Dynamic layout direction toggler (AR uses Rtl, EN/FR Ltr)
            val layoutDir = if (currentLang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                MyApplicationTheme(darkTheme = themeMode) {
                    HerdEscrowAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HerdEscrowAppContent(viewModel: EscrowViewModel) {
    val currentLoggedInUser by viewModel.currentLoggedInUser.collectAsStateWithLifecycle()
    val showSpec by viewModel.showSpecDialog.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentLoggedInUser == null) {
            OnboardingAuthScreen(viewModel = viewModel)
        } else if (currentLoggedInUser?.role.isNullOrEmpty()) {
            AccountTypeFormScreen(viewModel = viewModel)
        } else {
            MainDashboardScreen(viewModel = viewModel)
        }

        // Protocol Specifications Dialog
        if (showSpec) {
            ProtocolSpecDialog(
                language = lang,
                onDismiss = { viewModel.setSpecDialogVisible(false) }
            )
        }
    }
}

// -----------------------------------------------------
// ONBOARDING / WELCOME SCREEN (Dynamic Language + Persona Select)
// -----------------------------------------------------
@Composable
fun OnboardingAuthScreen(viewModel: EscrowViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Segmented tab state ("LOGIN" or "SIGNUP")
    var authTab by remember { mutableStateOf("LOGIN") }
    
    // Input Fields state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Error state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Minimalist Subtle Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Language.values().forEach { language ->
                    val isSelected = currentLang == language
                    OutlinedButton(
                        onClick = { viewModel.switchLanguage(language) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("lang_toggle_${language.code.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (language == Language.AR) "العربية" else if (language == Language.EN) "EN" else "FR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brand Logo Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐏",
                    fontSize = 38.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "حولي مضمون",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "TRIPARTITE SECURE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            Text(
                text = Translations.get("app_title", currentLang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )

            Text(
                text = Translations.get("app_tagline", currentLang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
            )

            // Segmented selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (authTab == "LOGIN") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { 
                            authTab = "LOGIN"
                            errorMessage = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Translations.get("login_tab", currentLang),
                        fontWeight = FontWeight.Bold,
                        color = if (authTab == "LOGIN") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (authTab == "SIGNUP") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { 
                            authTab = "SIGNUP"
                            errorMessage = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Translations.get("signup_tab", currentLang),
                        fontWeight = FontWeight.Bold,
                        color = if (authTab == "SIGNUP") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = Translations.get("username_lbl", currentLang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("input_username"),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("e.g. yassine_sardi", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Column {
                        Text(
                            text = Translations.get("password_lbl", currentLang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("input_password"),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("••••••••", fontSize = 14.sp) },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )
                    }

                    if (authTab == "SIGNUP") {
                        Column {
                            Text(
                                text = Translations.get("fullname_lbl", currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                modifier = Modifier.fillMaxWidth().testTag("input_fullName"),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text(if (currentLang == Language.AR) "حسن الفيلالي" else "Hassan Filali", fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Column {
                            Text(
                                text = Translations.get("phone_lbl", currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                modifier = Modifier.fillMaxWidth().testTag("input_phone"),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("+212 663-958472", fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (authTab == "LOGIN") {
                                viewModel.loginExistingUser(
                                    username = username,
                                    passwordRaw = password,
                                    onSuccess = {},
                                    onError = { errorMessage = it }
                                )
                            } else {
                                viewModel.registerNewUser(
                                    username = username,
                                    passwordRaw = password,
                                    fullName = fullName,
                                    phone = phone,
                                    onSuccess = {},
                                    onError = { errorMessage = it }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_auth"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (authTab == "LOGIN") Translations.get("login_tab", currentLang) else Translations.get("signup_tab", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tripartite info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = Translations.get("how_it_works", currentLang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepItem(text = Translations.get("step_1", currentLang), num = "١")
                        StepItem(text = Translations.get("step_2", currentLang), num = "٢")
                        StepItem(text = Translations.get("step_3", currentLang), num = "٣")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setSpecDialogVisible(true) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.get("how_it_works", currentLang) + " (Protocol Specs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AccountTypeFormScreen(viewModel: EscrowViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentLoggedInUser by viewModel.currentLoggedInUser.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var selectedRole by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = currentLoggedInUser?.fullName ?: currentLoggedInUser?.username ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = Translations.get("form_step_title", currentLang),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = Translations.get("form_step_desc", currentLang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 20.dp),
                lineHeight = 22.sp
            )

            // 3 Card Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FormRoleCard(
                    title = Translations.get("role_buyer", currentLang),
                    subtitle = if (currentLang == Language.AR) 
                        "شراء الأغنام وتأمين الثمن في حساب الضمان حتى الاستلام والتحقق من الوزن والصحة الفنية."
                        else "Buy premium livestock. Your money is locked securely in escrow until delivery verification.",
                    isSelected = selectedRole == "BUYER",
                    onClick = { 
                        selectedRole = "BUYER"
                        errorMsg = null
                    }
                )

                FormRoleCard(
                    title = Translations.get("role_farmer", currentLang),
                    subtitle = if (currentLang == Language.AR)
                        "عرض قطيع الكباش للبيع، الفحص البيطري الصادق، والحصول على مدفوعاتك بطريقة آمنة ومضمونة."
                        else "Offer your herds, fulfill photography-based health validations, and gain clear direct payouts.",
                    isSelected = selectedRole == "FARMER",
                    onClick = { 
                        selectedRole = "FARMER"
                        errorMsg = null
                    }
                )

                FormRoleCard(
                    title = Translations.get("role_mediator", currentLang),
                    subtitle = if (currentLang == Language.AR)
                        "صلاحيات التحكيم والإصلاح لحل أي نزاع طارئ وتأمين التداول الشفاف والعادل بين الطرفين."
                        else "Settle dispute flags and moderate secure transitions impartially matching platform specs.",
                    isSelected = selectedRole == "MEDIATOR",
                    onClick = { 
                        selectedRole = "MEDIATOR"
                        errorMsg = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cohesive Terms Check
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            termsAccepted = !termsAccepted 
                            errorMsg = null
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { 
                            termsAccepted = it 
                            errorMsg = null
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = Translations.get("form_terms", currentLang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedRole.isEmpty()) {
                        errorMsg = Translations.get("select_role_err", currentLang)
                        return@Button
                    }
                    if (!termsAccepted) {
                        errorMsg = Translations.get("accept_terms_err", currentLang)
                        return@Button
                    }
                    viewModel.saveUserRoleSelection(selectedRole) {
                        // Success
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_role_selection"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = Translations.get("form_confirm_btn", currentLang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FormRoleCard(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            2.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isSelected) 0.85f else 0.6f),
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StepItem(text: String, num: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = num,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun RoleSelectionCard(title: String, subtitle: String, tag: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("role_select_$tag"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------
// MAIN DASHBOARD SCREEN (Multi-Persona Tabs)
// -----------------------------------------------------
@Composable
fun MainDashboardScreen(viewModel: EscrowViewModel) {
    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(activeRole) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "حولي مضمون 🐏",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = when (activeRole) {
                                    "BUYER" -> if (currentLang == Language.AR) "زبون" else "Buyer"
                                    "FARMER" -> if (currentLang == Language.AR) "مربي" else "Farmer"
                                    else -> if (currentLang == Language.AR) "وسيط" else "Mediator"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setSpecDialogVisible(true) }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Specs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("action_switch_persona")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out / Sign Out",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            val tabs = when (activeRole) {
                "BUYER" -> listOf(
                    Translations.get("tab_browse", currentLang),
                    Translations.get("tab_buyer_dashboard", currentLang)
                )
                "FARMER" -> listOf(
                    Translations.get("tab_browse", currentLang),
                    Translations.get("tab_farmer_space", currentLang)
                )
                else -> listOf( // MEDIATOR
                    Translations.get("tab_browse", currentLang),
                    Translations.get("tab_disputes", currentLang)
                )
            }

            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        modifier = Modifier.testTag("nav_tab_$index"),
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Home else {
                                    when (activeRole) {
                                        "BUYER" -> Icons.Default.ShoppingCart
                                        "FARMER" -> Icons.Default.Add
                                        else -> Icons.Default.Warning
                                    }
                                },
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> BrowseListingsTab(viewModel = viewModel)
                1 -> {
                    when (activeRole) {
                        "BUYER" -> BuyerDashboardTab(viewModel = viewModel)
                        "FARMER" -> FarmerToolsTab(viewModel = viewModel)
                        "MEDIATOR" -> MediatorDisputeTab(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun EscrowDashboardCard(viewModel: EscrowViewModel) {
    val transactions by viewModel.escrowTransactions.collectAsStateWithLifecycle()
    val disputes by viewModel.escrowDisputes.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val totalBalance = remember(transactions) {
        transactions.filter { it.escrowStatus == "HELD_IN_ESCROW" || it.escrowStatus == "UNDER_DISPUTE" }
            .sumOf { it.totalAmount }
    }
    
    val pendingCount = remember(transactions) {
        transactions.filter { it.escrowStatus == "HELD_IN_ESCROW" }.size
    }

    val disputeCount = remember(disputes) {
        disputes.filter { it.status == "OPEN" }.size
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize().matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.35f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentLang == Language.AR) "حساب الضمان المشترك" else "Secured Escrow Volume",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLang == Language.AR) "${String.format("%,.0f", totalBalance)} درهم" else "${String.format("%,.0f", totalBalance)} DH",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                    
                    // Secure Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "SECURE 🛡️",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pending Cell
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "↑", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$pendingCount Order${if (pendingCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Disputes Cell
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Disputes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$disputeCount Active",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------
// TAB 1: BROWSE MARKET LISTINGS (Shared View)
// -----------------------------------------------------
@Composable
fun BrowseListingsTab(viewModel: EscrowViewModel) {
    val listings by viewModel.ramListings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegion.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var activeDetailsListing by remember { mutableStateOf<RamListing?>(null) }
    val regions = listOf("All", "Settat", "Khénifra", "Figuig", "Errachidia", "Boujaâd")

    val filteredListings = listings.filter { listing ->
        val breedMatches = listing.breed.contains(searchQuery, ignoreCase = true) ||
                listing.sellerName.contains(searchQuery, ignoreCase = true)
        val regionMatches = if (selectedRegion == "All") true else {
            listing.location.contains(selectedRegion, ignoreCase = true)
        }
        breedMatches && regionMatches
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EscrowDashboardCard(viewModel = viewModel)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_field"),
                        placeholder = {
                            Text(
                                text = Translations.get("search_placeholder", currentLang),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search"
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )
                }

                Text(
                    text = Translations.get("filter_region", currentLang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    regions.forEach { region ->
                        val isSelected = selectedRegion == region
                        val localizedRegionName = when (region) {
                            "All" -> Translations.get("all_regions", currentLang)
                            "Settat" -> if (currentLang == Language.AR) "سطات" else if (currentLang == Language.FR) "Settat" else "Settat"
                            "Khénifra" -> if (currentLang == Language.AR) "خنيفرة" else if (currentLang == Language.FR) "Khénifra" else "Khenifra"
                            "Figuig" -> if (currentLang == Language.AR) "فكيك" else if (currentLang == Language.FR) "Figuig" else "Figuig"
                            "Errachidia" -> if (currentLang == Language.AR) "الرشيدية" else if (currentLang == Language.FR) "Errachidia" else "Errachidia"
                            "Boujaâd" -> if (currentLang == Language.AR) "بوجعد" else if (currentLang == Language.FR) "Boujaâd" else "Boujaad"
                            else -> region
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSelectedRegion(region) },
                            modifier = Modifier.testTag("filter_chip_$region"),
                            label = {
                                Text(
                                    text = localizedRegionName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.Transparent,
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = isSelected,
                                enabled = true,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (filteredListings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🐏", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLang == Language.AR) "لا توجد نتائج سلالات مطابقة للفلاتر النشطة" else "No matching ram listings found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredListings, key = { it.id }) { item ->
                        RamListingCard(
                            listing = item,
                            language = currentLang,
                            onClick = { activeDetailsListing = item }
                        )
                    }
                }
            }
        }

        if (activeDetailsListing != null) {
            RamDetailsSheet(
                listing = activeDetailsListing!!,
                language = currentLang,
                viewModel = viewModel,
                onDismiss = { activeDetailsListing = null }
            )
        }
    }
}

@Composable
fun RamListingCard(listing: RamListing, language: Language, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ram_card_${listing.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Column {
            val context = androidx.compose.ui.platform.LocalContext.current
            val imageResId = remember(listing.imageUrl) {
                if (listing.imageUrl.isNotEmpty()) {
                    context.resources.getIdentifier(listing.imageUrl, "drawable", context.packageName)
                } else 0
            }

            val imageBgColor = remember(listing.imageColorHex) {
                try {
                    Color(android.graphics.Color.parseColor(listing.imageColorHex))
                } catch (e: Exception) {
                    Color(0xFFFAF9F6)
                }
            }

            // Top Visual Image Container with Absolute Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(imageBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = imageResId),
                        contentDescription = listing.breed,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (listing.imageUrl.isNotEmpty()) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(model = listing.imageUrl),
                        contentDescription = listing.breed,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Background subtle patterns
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 2f
                        )
                    }

                    // Centered Cute Ram Mascot
                    Text(
                        text = "🐏",
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Top-Left Absolute "Certified Farm" Overlay Badge
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (language == Language.AR) "مضمون 🛡️" else "Certified",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Bottom-Right Absolute Weight Overlay Badge
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${listing.weightKg} kg",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Descriptive Information Block with modern negative spacing
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = listing.breed,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listing.sellerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (listing.earTagNumber.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🏷️",
                            fontSize = 8.sp
                        )
                        Text(
                            text = listing.earTagNumber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == Language.AR) "${listing.price.toInt()} درهم" else "${listing.price.toInt()} DH",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = listing.location.split("(").first().trim(),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------
// MODAL: EXPANDED DETAILS / INTERACTIVE ESCROW CHECKOUT
// -----------------------------------------------------
@Composable
fun RamDetailsSheet(
    listing: RamListing,
    language: Language,
    viewModel: EscrowViewModel,
    onDismiss: () -> Unit
) {
    var showCheckout by remember { mutableStateOf(false) }
    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.get("details", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!showCheckout) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val imageResId = remember(listing.imageUrl) {
                        if (listing.imageUrl.isNotEmpty()) {
                            context.resources.getIdentifier(listing.imageUrl, "drawable", context.packageName)
                        } else 0
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(listing.imageColorHex))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageResId != 0) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = imageResId),
                                contentDescription = listing.breed,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (listing.imageUrl.isNotEmpty()) {
                            androidx.compose.foundation.Image(
                                painter = coil.compose.rememberAsyncImagePainter(model = listing.imageUrl),
                                contentDescription = listing.breed,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "🐏", fontSize = 48.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = listing.breed,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DetailField(label = Translations.get("seller", language), value = listing.sellerName)
                    DetailField(label = Translations.get("weight", language), value = "${listing.weightKg} ${Translations.get("weight", language).takeLast(4).replace(":", "").trim()}")
                    if (listing.earTagNumber.isNotEmpty()) {
                        DetailField(label = Translations.get("ear_tag_label", language), value = listing.earTagNumber)
                    }
                    DetailField(label = Translations.get("location", language), value = listing.location)
                    DetailField(label = Translations.get("quantity_avail", language), value = "${listing.quantityAvailable} heads")
                    DetailField(label = Translations.get("price", language), value = if (language == Language.AR) "${listing.price.toInt()} د.م. (مضمون بالكامل)" else "${listing.price.toInt()} DH (Locked Safely)")

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Escrow Lock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Translations.get("checkout_subtitle", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (activeRole == "BUYER") {
                        Button(
                            onClick = { if (listing.quantityAvailable > 0) showCheckout = true },
                            enabled = listing.quantityAvailable > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_trigger_checkout"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = Translations.get("buy_now", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (language == Language.AR) "الدخول بهوية المشتري مطلوب للشراء والتداول الائتماني." else "Login as Buyer to secure this item in escrow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                } else {
                    CheckoutForm(
                        listing = listing,
                        language = language,
                        viewModel = viewModel,
                        onSuccess = {
                            onDismiss()
                        },
                        onBack = { showCheckout = false }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    }
}

// -----------------------------------------------------
// CHECKOUT FORM (With complete address phone state checks)
// -----------------------------------------------------
@Composable
fun CheckoutForm(
    listing: RamListing,
    language: Language,
    viewModel: EscrowViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cardNum by remember { mutableStateOf("") }
    var expires by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (language == Language.AR) "رجوع لتفاصيل الفحل" else "Back to Details",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = Translations.get("checkout_title", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = buyerName,
            onValueChange = { buyerName = it },
            label = { Text(Translations.get("buyer_name", language)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_buyer_name"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = buyerPhone,
            onValueChange = { buyerPhone = it },
            label = { Text(Translations.get("buyer_phone", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_buyer_phone"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(Translations.get("delivery_address", language)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_delivery_address"),
            shape = RoundedCornerShape(12.dp),
            singleLine = false,
            maxLines = 2
        )

        Text(
            text = Translations.get("card_info", language),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = cardNum,
            onValueChange = { if (it.length <= 16) cardNum = it },
            label = { Text(Translations.get("card_num", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_card_num"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = expires,
                onValueChange = { if (it.length <= 5) expires = it },
                label = { Text("MM/YY") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_card_expiry"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 3) cvv = it },
                label = { Text("CVV") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_card_cvv"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (buyerName.isNotBlank() && buyerPhone.isNotBlank() && address.isNotBlank() && cardNum.length == 16) {
                    viewModel.placeFundsInEscrow(
                        listingId = listing.id,
                        breed = listing.breed,
                        seller = listing.sellerName,
                        buyerName = buyerName,
                        buyerPhone = buyerPhone,
                        address = address,
                        quantity = 1,
                        totalPrice = listing.price,
                        cardNumber16 = cardNum,
                        onSuccess = {
                            Toast.makeText(context, Translations.get("toast_success_escrow", language), Toast.LENGTH_LONG).show()
                            onSuccess()
                        }
                    )
                } else {
                    Toast.makeText(context, Translations.get("toast_fill_fields", language), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_checkout_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = Translations.get("confirm_escrow_deposit", language),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// -----------------------------------------------------
// TAB 2 (BUYER): MY ACTIONS & ORDERS PANEL
// -----------------------------------------------------
@Composable
fun BuyerDashboardTab(viewModel: EscrowViewModel) {
    val transactions by viewModel.escrowTransactions.collectAsStateWithLifecycle()
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTxForDispute by remember { mutableStateOf<EscrowTransaction?>(null) }

    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "📭", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Translations.get("no_transactions", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    BuyerTransactionCard(
                        tx = tx,
                        language = language,
                        onRelease = {
                            viewModel.confirmDeliveryAndRelease(tx.id) {
                                Toast.makeText(context, "released payout successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRaiseDispute = {
                            selectedTxForDispute = tx
                        }
                    )
                }
            }

            if (selectedTxForDispute != null) {
                DisputeFilingDialog(
                    tx = selectedTxForDispute!!,
                    language = language,
                    viewModel = viewModel,
                    onDismiss = { selectedTxForDispute = null }
                )
            }
        }
    }
}

@Composable
fun BuyerTransactionCard(
    tx: EscrowTransaction,
    language: Language,
    onRelease: () -> Unit,
    onRaiseDispute: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_card_${tx.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.ramBreed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val (statusLabel, chipBg, chipText) = when (tx.escrowStatus) {
                    "HELD_IN_ESCROW" -> Triple(
                        Translations.get("status_held", language),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.primary
                    )
                    "RELEASED" -> Triple(
                        Translations.get("status_released", language),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.tertiary
                    )
                    "UNDER_DISPUTE" -> Triple(
                        Translations.get("status_disputed", language),
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.error
                    )
                    else -> Triple(
                        Translations.get("status_refunded", language),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.secondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = chipText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${Translations.get("seller", language)} ${tx.sellerName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = "📍 ${tx.deliveryAddress} | 📞 ${tx.buyerPhone}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Translations.get("seller_payout", language),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = if (language == Language.AR) "${tx.totalAmount.toInt()} د.م. (بطاقة **** ${tx.paymentCardLast4})" else "${tx.totalAmount.toInt()} DH (Card **** ${tx.paymentCardLast4})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (tx.escrowStatus == "HELD_IN_ESCROW") {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRelease,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_release_${tx.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text(
                            text = Translations.get("action_confirm_delivery", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onRaiseDispute,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_dispute_${tx.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = Translations.get("action_raise_dispute", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------
// DISPUTE FILING DIALOG MODAL
// -----------------------------------------------------
@Composable
fun DisputeFilingDialog(
    tx: EscrowTransaction,
    language: Language,
    viewModel: EscrowViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var disputeReason by remember { mutableStateOf("WEIGHT_DISCREPANCY") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.get("dispute_modal_title", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = Translations.get("dispute_reason", language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = disputeReason == "WEIGHT_DISCREPANCY",
                                onClick = { disputeReason = "WEIGHT_DISCREPANCY" }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = disputeReason == "WEIGHT_DISCREPANCY",
                            onClick = { disputeReason = "WEIGHT_DISCREPANCY" },
                            modifier = Modifier.testTag("radio_weight")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translations.get("reason_weight", language), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = disputeReason == "HEALTH_ISSUES",
                                onClick = { disputeReason = "HEALTH_ISSUES" }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = disputeReason == "HEALTH_ISSUES",
                            onClick = { disputeReason = "HEALTH_ISSUES" },
                            modifier = Modifier.testTag("radio_health")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translations.get("reason_health", language), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = disputeReason == "DELIVERY_DELAY",
                                onClick = { disputeReason = "DELIVERY_DELAY" }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = disputeReason == "DELIVERY_DELAY",
                            onClick = { disputeReason = "DELIVERY_DELAY" },
                            modifier = Modifier.testTag("radio_delay")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translations.get("reason_delay", language), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Translations.get("dispute_desc", language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(Translations.get("desc_hint", language), fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("input_dispute_desc"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            viewModel.raiseDispute(tx.id, disputeReason, description) {
                                Toast.makeText(context, "Dispute raised successfully! Intermediate Escrow Locked.", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        } else {
                            Toast.makeText(context, Translations.get("toast_fill_fields", language), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_dispute_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Translations.get("submit_dispute", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
// TAB 2 (FARMER): NEW RAM LISTING CREATOR & VET CAMERA SIMULATOR
// -----------------------------------------------------
@Composable
fun FarmerToolsTab(viewModel: EscrowViewModel) {
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isCapturing by viewModel.isVetCapturing.collectAsStateWithLifecycle()
    val progress by viewModel.vetCaptureProgress.collectAsStateWithLifecycle()
    val isCertified by viewModel.isVetCertified.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var breed by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var sellerFarmName by remember { mutableStateOf("") }
    var earTagNumber by remember { mutableStateOf("") }

    // Manual simulator viewfinder states
    var showCameraViewfinder by remember { mutableStateOf(false) }
    var isFlashActive by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisProgress by remember { mutableStateOf(0f) }

    // Selected Gallery Image and proofing states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isScanningGalleryImage by remember { mutableStateOf(false) }
    var galleryProgress by remember { mutableStateOf(0f) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.forceVetCertifiedStatus(false)
            isScanningGalleryImage = true
            galleryProgress = 0f
            showCameraViewfinder = false
            scope.launch {
                for (i in 1..10) {
                    delay(150)
                    galleryProgress = i / 10f
                }
                isScanningGalleryImage = false
                viewModel.forceVetCertifiedStatus(true)
                Toast.makeText(context, Translations.get("photo_certified", language), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cardColors = listOf("#FAF9F6", "#CF7B3C", "#E6B272", "#0F6F47", "#1BB275")
    var selectedColorHex by remember { mutableStateOf("#FAF9F6") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = Translations.get("farmer_add_listing", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (language == Language.AR) "سلالات الأغنام المغربية الأصيلة المتاحة (اضغط للتعبئة التلقائية):" else "Classic Moroccan Ram Breeds (Tap to Auto-Fill):",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        val moroccanBreeds = listOf(
            Triple("الصردي", "Sardi", Triple(85.0, 4800.0, Pair("سطات (Settat)", "ONSSA-SRD-2026-"))),
            Triple("تمحضيت", "Timahdite", Triple(74.0, 3200.0, Pair("خنيفرة (Khénifra)", "ONSSA-TMH-2026-"))),
            Triple("بني غيل", "Beni Guil", Triple(78.0, 3800.0, Pair("فكيك (Figuig)", "ONSSA-BNG-2026-"))),
            Triple("الدمان", "D'man", Triple(61.0, 2600.0, Pair("الرشيدية (Errachidia)", "ONSSA-DMN-2026-"))),
            Triple("بوجعد", "Boujaâd", Triple(68.0, 3000.0, Pair("بوجعد (Boujaâd)", "ONSSA-BJD-2026-")))
        )

        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            items(moroccanBreeds.size) { index ->
                val (arName, enName, data) = moroccanBreeds[index]
                val (weightVal, priceVal, extra) = data
                val (locVal, tagPref) = extra
                val isSelected = breed.contains(arName) || breed.contains(enName)

                Surface(
                    onClick = {
                        breed = if (language == Language.AR) "سلالة $arName الأصيلة ($enName Breed)" else "$enName Pure Breed ($arName)"
                        weightStr = weightVal.toInt().toString()
                        priceStr = priceVal.toInt().toString()
                        location = locVal
                        earTagNumber = tagPref + (1000..9999).random().toString()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("breed_chip_$enName")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🐏", fontSize = 14.sp)
                        Column {
                            Text(
                                text = if (language == Language.AR) arName else enName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (language == Language.AR) "${priceVal.toInt()} درهم" else "${priceVal.toInt()} DH",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text(Translations.get("breed_label", language)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_farmer_breed"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = weightStr,
            onValueChange = { weightStr = it },
            label = { Text(Translations.get("weight_input", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_farmer_weight"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(Translations.get("location_input", language)) },
            placeholder = { Text("e.g. Settat, Khénifra, Figuig") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_farmer_location"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text(Translations.get("price_input", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_farmer_price"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = sellerFarmName,
            onValueChange = { sellerFarmName = it },
            label = { Text(Translations.get("farm_name", language)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_farmer_seller"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = earTagNumber,
            onValueChange = { earTagNumber = it },
            label = { Text(Translations.get("ear_tag_label", language)) },
            placeholder = { Text(Translations.get("ear_tag_placeholder", language)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_farmer_eartag"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Text(
            text = if (language == Language.AR) "اختر لون خلفية البطاقة الرقمية:" else "Select Digital Card background accent color:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            cardColors.forEach { hx ->
                val col = Color(android.graphics.Color.parseColor(hx))
                val isSelected = selectedColorHex == hx
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(col)
                        .border(
                            if (isSelected) 3.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedColorHex = hx }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = Translations.get("vet_certification", language),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (showCameraViewfinder) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interactive_camera_viewfinder")
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Translations.get("camera_man_title", language),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = Translations.get("camera_tap_to_capture", language),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    // Finder window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF151515), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Subdivisions grid
                            drawLine(
                                color = Color.White.copy(alpha = 0.15f),
                                start = androidx.compose.ui.geometry.Offset(size.width / 3f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width / 3f, size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.15f),
                                start = androidx.compose.ui.geometry.Offset(size.width * 2 / 3f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width * 2 / 3f, size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.15f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 3f),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 3f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.15f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height * 2 / 3f),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 2 / 3f),
                                strokeWidth = 1f
                            )

                            // Focus brackets
                            val bLen = 24.dp.toPx()
                            val strokeW = 3f
                            val p = 20.dp.toPx()
                            // Top left
                            drawArc(
                                color = Color(0xFFFFD700),
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
                                size = androidx.compose.ui.geometry.Size(bLen, bLen),
                                topLeft = androidx.compose.ui.geometry.Offset(p, p)
                            )
                            // Top right
                            drawArc(
                                color = Color(0xFFFFD700),
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
                                size = androidx.compose.ui.geometry.Size(bLen, bLen),
                                topLeft = androidx.compose.ui.geometry.Offset(size.width - bLen - p, p)
                            )
                            // Bottom left
                            drawArc(
                                color = Color(0xFFFFD700),
                                startAngle = 90f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
                                size = androidx.compose.ui.geometry.Size(bLen, bLen),
                                topLeft = androidx.compose.ui.geometry.Offset(p, size.height - bLen - p)
                            )
                            // Bottom right
                            drawArc(
                                color = Color(0xFFFFD700),
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
                                size = androidx.compose.ui.geometry.Size(bLen, bLen),
                                topLeft = androidx.compose.ui.geometry.Offset(size.width - bLen - p, size.height - bLen - p)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🐏", fontSize = 48.sp)
                            Text(
                                text = "ONSSA TAG SCANNER",
                                color = Color(0xFFFFD700),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Text("ISO 400", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("F/2.0", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "HEALTH STATUS: EXCELLENT • SECURE ID BINDING",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp)
                        )

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isFlashActive,
                            enter = androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                            )
                        }
                    }

                    if (isAnalyzing) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { analysisProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("vet_progress_indicator"),
                                color = Color(0xFFFFD700),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            Text(
                                text = if (language == Language.AR) "جاري التحقق الرقمي والتوثيق..." else "Digital Verification & Cryptographic Binding...",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Manual tactile shutter circle
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    scope.launch {
                                        isFlashActive = true
                                        delay(100)
                                        isFlashActive = false
                                        isAnalyzing = true
                                        analysisProgress = 0f
                                        for (i in 1..10) {
                                            delay(100)
                                            analysisProgress = i / 10f
                                        }
                                        isAnalyzing = false
                                        showCameraViewfinder = false
                                        viewModel.forceVetCertifiedStatus(true)
                                        Toast.makeText(context, Translations.get("photo_certified", language), Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(4.dp)
                                .border(4.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                        }
                    }
                }
            }
        } else if (selectedImageUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gallery_image_container")
                    .border(1.dp, if (isCertified) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (language == Language.AR) "معاينة وتحليل صورة المعرض 🖼️" else "Gallery Image Audit Preview 🖼️",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = coil.compose.rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Selected sheep photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        if (isScanningGalleryImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                            )

                            // Moving laser scan line
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val y = size.height * galleryProgress
                                drawLine(
                                    color = Color(0xFFFFD700),
                                    start = androidx.compose.ui.geometry.Offset(0f, y),
                                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                                    strokeWidth = 6f
                                )
                            }

                            Text(
                                text = "ANALYSIS IN PROGRESS...",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        } else if (isCertified) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color(0xFF1BB275).copy(alpha = 0.9f))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PROVENANCE REGISTERED",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    if (isScanningGalleryImage) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { galleryProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("vet_progress_indicator"),
                                color = Color(0xFFFFD700),
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            Text(
                                text = if (language == Language.AR) "جاري التحقق والمطابقة الضامنة..." else "Interpreting image biometrics...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else if (isCertified) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Translations.get("photo_certified", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedImageUri = null
                                        viewModel.forceVetCertifiedStatus(false)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (language == Language.AR) "إزالة الصورة 🗑️" else "Remove Photo 🗑️", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch("image/*")
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(Translations.get("camera_retake", language), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isCertified) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    showCameraViewfinder = true
                                    selectedImageUri = null
                                },
                                modifier = Modifier.weight(1f).testTag("btn_vet_camera"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == Language.AR) "تشغيل الكاميرا 📸" else "Camera 📸",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = {
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1.5f).testTag("btn_gallery_picker"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == Language.AR) "اختيار من المعرض 🖼️" else "Select from Gallery 🖼️",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Translations.get("photo_certified", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.forceVetCertifiedStatus(false)
                                    showCameraViewfinder = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(Translations.get("camera_retake", language), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val weightVal = weightStr.toDoubleOrNull()
                val priceVal = priceStr.toDoubleOrNull()
                if (breed.isNotBlank() && weightVal != null && location.isNotBlank() && priceVal != null && sellerFarmName.isNotBlank() && isCertified) {
                    viewModel.publishRamListing(
                        breed = breed,
                        weight = weightVal,
                        location = location,
                        price = priceVal,
                        seller = sellerFarmName,
                        colorHex = selectedColorHex,
                        earTagNumber = earTagNumber,
                        customImageUrl = selectedImageUri?.toString(),
                        onSuccess = {
                            Toast.makeText(context, Translations.get("toast_listing_published", language), Toast.LENGTH_LONG).show()
                            breed = ""
                            weightStr = ""
                            location = ""
                            priceStr = ""
                            sellerFarmName = ""
                            earTagNumber = ""
                            selectedImageUri = null
                        }
                    )
                } else {
                    Toast.makeText(
                        context,
                        if (!isCertified) "الفحص البيطري بالصورة مطلوب لإتمام النشر!" else Translations.get("toast_fill_fields", language),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_publish_listing"),
            enabled = breed.isNotBlank() && weightStr.isNotBlank() && location.isNotBlank() && priceStr.isNotBlank() && sellerFarmName.isNotBlank() && isCertified,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        ) {
            Text(
                text = Translations.get("publish_listing", language),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// -----------------------------------------------------
// TAB 2 (MEDIATOR): RESOLUTIONS & DISPUTES CENTER
// -----------------------------------------------------
@Composable
fun MediatorDisputeTab(viewModel: EscrowViewModel) {
    val disputes by viewModel.escrowDisputes.collectAsStateWithLifecycle()
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openDisputes = disputes.filter { it.status == "OPEN" }

    if (openDisputes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "⚖️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Translations.get("no_disputes", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = Translations.get("mediator_panel_title", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(openDisputes, key = { it.id }) { item ->
                DisputeArbitrationCard(
                    dispute = item,
                    language = language,
                    onResolveRefundBuyer = {
                        viewModel.arbitrateDispute(item.id, item.transactionId, payoutToFarmer = false) {
                            Toast.makeText(context, Translations.get("toast_arbitrated", language), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onResolveReleaseSeller = {
                        viewModel.arbitrateDispute(item.id, item.transactionId, payoutToFarmer = true) {
                            Toast.makeText(context, Translations.get("toast_arbitrated", language), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DisputeArbitrationCard(
    dispute: EscrowDispute,
    language: Language,
    onResolveRefundBuyer: () -> Unit,
    onResolveReleaseSeller: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dispute_card_${dispute.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Translations.get("dispute_ticket", language)}${dispute.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = dispute.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = dispute.ramBreed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            DisputeSummaryField(label = Translations.get("dispute_by", language), value = dispute.buyerName)
            DisputeSummaryField(label = if (language == Language.AR) "المربي المشتكى عليه:" else "Accused Farmer:", value = dispute.sellerName)
            DisputeSummaryField(label = Translations.get("dispute_reason_label", language), value = Translations.get(
                when (dispute.disputeReason) {
                    "WEIGHT_DISCREPANCY" -> "reason_weight"
                    "HEALTH_ISSUES" -> "reason_health"
                    else -> "reason_delay"
                },
                language
            ))
            DisputeSummaryField(label = Translations.get("seller_payout", language), value = if (language == Language.AR) "${dispute.totalAmount.toInt()} درهم" else "${dispute.totalAmount.toInt()} DH")

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (language == Language.AR) "تظلم المشتري ومستنداته:" else "Buyer grievance/claim details:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = dispute.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = Translations.get("arbitrate_action", language),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onResolveRefundBuyer,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_arbitrate_refund_${dispute.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = if (language == Language.AR) "رد المشتري" else "Refund Buyer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onResolveReleaseSeller,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_arbitrate_release_${dispute.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (language == Language.AR) "صرف للمربي" else "Release Farmer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DisputeSummaryField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// -----------------------------------------------------
// FLOATING MODAL: SECURITY ARCHITECTURE PROTOCOL SPECIFICATIONS
// -----------------------------------------------------
@Composable
fun ProtocolSpecDialog(language: Language, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛡️",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.get("spec_dialog_title", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SpecFactItem(title = "Tripartite Hold State", desc = "Mutually independent smart-hold. Funds locked under state transaction status.")
                        SpecFactItem(title = "Room Database Single Truth", desc = "Entities fully correlated with auto-cascade schemas.")
                        SpecFactItem(title = "Clinical Vet Telemetry", desc = "Automatic camera scans certifies listings prior to active market propagation.")
                        SpecFactItem(title = "Role-Isolation Security", desc = "Views are role-isolated preventing un-authorized access to dispute center and farmer publish interfaces.")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = Translations.get("spec_paragraph", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = Translations.get("close", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpecFactItem(title: String, desc: String) {
    Column {
        Text(text = "• $title", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 10.dp))
    }
}
