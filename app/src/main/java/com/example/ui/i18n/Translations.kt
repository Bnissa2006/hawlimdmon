package com.example.ui.i18n

enum class Language(val code: String, val displayName: String, val rtl: Boolean) {
    AR("AR", "العربية (Arabic)", true),
    EN("EN", "English", false),
    FR("FR", "Français (French)", false)
}

object Translations {
    private val data = mapOf(
        "app_title" to mapOf(
            Language.AR to "نظام التداول الثلاثي الآمن للمواشي والأغنام 🐏",
            Language.EN to "Secure Tripartite Livestock Escrow System 🐏",
            Language.FR to "Système d'Escrow Tripartite Sécurisé de Bétail 🐏"
        ),
        "app_tagline" to mapOf(
            Language.AR to "حماية تامة للمشتري، ضمان الدفع للمربي، وتحكيم عادل من الوسيط",
            Language.EN to "Complete buyer protection, guaranteed seller payout, fair mediator arbitration",
            Language.FR to "Protection totale de l'acheteur, paiement garanti à l'éleveur, arbitrage équitable"
        ),
        "onboarding_welcome" to mapOf(
            Language.AR to "مرحباً بك في حولي مضمون",
            Language.EN to "Welcome to Hawli Madmon",
            Language.FR to "Bienvenue sur Hawli Madmon"
        ),
        "role_buyer" to mapOf(
            Language.AR to "مشتري / زبون 🛒",
            Language.EN to "Buyer / Customer 🛒",
            Language.FR to "Acheteur / Client 🛒"
        ),
        "role_farmer" to mapOf(
            Language.AR to "مربي / بائع 🌿",
            Language.EN to "Farmer / Seller 🌿",
            Language.FR to "Éleveur / Vendeur 🌿"
        ),
        "role_mediator" to mapOf(
            Language.AR to "وسيط / مصلح ⚖️",
            Language.EN to "Mediator / Support ⚖️",
            Language.FR to "Médiateur / Support ⚖️"
        ),
        "choose_role_title" to mapOf(
            Language.AR to "اختر هويتك للدخول إلى لوحة التحكم المخصصة:",
            Language.EN to "Choose your active role to access your dashboard:",
            Language.FR to "Choisissez votre rôle actif pour accéder au tableau de bord:"
        ),
        "how_it_works" to mapOf(
            Language.AR to "كيف يعمل الضمان الثلاثي؟",
            Language.EN to "How Tripartite Escrow Works?",
            Language.FR to "Comment fonctionne l'escrow tripartite ?"
        ),
        "step_1" to mapOf(
            Language.AR to "1. يختار المشتري الأضحية ويقوم بحجز الأموال في محفظة الضمان الآمنة (Held in Escrow).",
            Language.EN to "1. Buyer selects a sheep and locks purchasing funds securely in escrow storage.",
            Language.FR to "1. L'acheteur choisit le bélier et bloque les fonds dans l'espace d'escrow sécurisé."
        ),
        "step_2" to mapOf(
            Language.AR to "2. يبدأ المربي بالتحضير وتوصيل الماشية إلى المشتري بوزنها وسلامتها الطبية المعتمدة.",
            Language.EN to "2. Farmer prepares and delivers the livestock matching certified weight and health guidelines.",
            Language.FR to "2. L'éleveur prépare et livre le bétail selon le poids et les consignes vétérinaires."
        ),
        "step_3" to mapOf(
            Language.AR to "3. عند الاستلام والمطابقة، يفرج المشتري عن الأموال. في حال الخلاف، يتدخل الوسيط للإصلاح والتحكيم.",
            Language.EN to "3. Buyer releases escrow on arrival. In case of issues, mediator arbitrates with proof.",
            Language.FR to "3. L'acheteur libère l'escrow. En cas de litige, le médiateur arbitre selon les preuves."
        ),
        "enter_app" to mapOf(
            Language.AR to "دخول النظام 🚀",
            Language.EN to "Enter System 🚀",
            Language.FR to "Entrer dans le système 🚀"
        ),
        "switch_lang" to mapOf(
            Language.AR to "تغيير اللغة",
            Language.EN to "Switch Language",
            Language.FR to "Changer de langue"
        ),
        "search_placeholder" to mapOf(
            Language.AR to "ابحث عن السلالة (الصردي، تمحضيت، بني غيل، الدمان...)",
            Language.EN to "Search breed (Sardi, Timahdite, Beni Guil, D'man...)",
            Language.FR to "Rechercher une race (Sardi, Timahdite, Beni Guil...)"
        ),
        "filter_region" to mapOf(
            Language.AR to "تصفية حسب الجهة / المدينة:",
            Language.EN to "Filter by Region / City:",
            Language.FR to "Filtrer par Région / Ville :"
        ),
        "all_regions" to mapOf(
            Language.AR to "الكل",
            Language.EN to "All Regions",
            Language.FR to "Toutes"
        ),
        "weight" to mapOf(
            Language.AR to "الوزن المعتمد:",
            Language.EN to "Certified Weight:",
            Language.FR to "Poids certifié :"
        ),
        "location" to mapOf(
            Language.AR to "الموقع / المدينة:",
            Language.EN to "Location / City:",
            Language.FR to "Localisation / Ville :"
        ),
        "price" to mapOf(
            Language.AR to "السعر والضمان:",
            Language.EN to "Price in Escrow:",
            Language.FR to "Prix sous escrow :"
        ),
        "seller" to mapOf(
            Language.AR to "المربي المسؤل:",
            Language.EN to "Responsible Farmer:",
            Language.FR to "Éleveur responsable :"
        ),
        "quantity_avail" to mapOf(
            Language.AR to "الرؤوس المتاحة:",
            Language.EN to "Available Rams:",
            Language.FR to "Béliers disponibles :"
        ),
        "buy_now" to mapOf(
            Language.AR to "شراء وإيداع في الضمان 💳",
            Language.EN to "Lock Funds in Escrow 💳",
            Language.FR to "Bloquer les fonds (Escrow) 💳"
        ),
        "details" to mapOf(
            Language.AR to "التفاصيل والطلب 🎯",
            Language.EN to "Details & Order 🎯",
            Language.FR to "Détails & Commande 🎯"
        ),
        "checkout_title" to mapOf(
            Language.AR to "إتمام عملية التداول الثلاثي الآمن",
            Language.EN to "Complete Secure Tripartite Transaction",
            Language.FR to "Compléter la transaction tripartite"
        ),
        "checkout_subtitle" to mapOf(
            Language.AR to "سيتم قفل أموالك بأمان في محفظة الضمان ولا يتم تحريرها إلا بعد استلام الأضحية ومطابقتها.",
            Language.EN to "Your funds will be safely locked in our smart escrow account and released only after delivery confirmation.",
            Language.FR to "Vos fonds seront bloqués dans le compte d'escrow et libérés après confirmation de livraison."
        ),
        "buyer_name" to mapOf(
            Language.AR to "اسم المشتري الكامل:",
            Language.EN to "Full Buyer Name:",
            Language.FR to "Nom complet de l'acheteur :"
        ),
        "buyer_phone" to mapOf(
            Language.AR to "رقم الهاتف للتوصيل:",
            Language.EN to "Delivery Phone Number:",
            Language.FR to "Numéro de téléphone :"
        ),
        "delivery_address" to mapOf(
            Language.AR to "عنوان تسليم الأضحية بالتفصيل:",
            Language.EN to "Detailed Delivery Address:",
            Language.FR to "Adresse de livraison détaillée :"
        ),
        "card_info" to mapOf(
            Language.AR to "تفاصيل بطاقة الدفع (محاكاة آمنة):",
            Language.EN to "Payment Card Details (Simulated):",
            Language.FR to "Carte de paiement (Simulée) :"
        ),
        "card_num" to mapOf(
            Language.AR to "رقم البطاقة الائتمانية (16 رقماً):",
            Language.EN to "Credit Card Number (16 digits):",
            Language.FR to "Numéro de carte (16 chiffres) :"
        ),
        "card_expiry" to mapOf(
            Language.AR to "تاريخ انتهاء الصلاحية والشفرة:",
            Language.EN to "Expiry & CVV:",
            Language.FR to "Expiration & CVV :"
        ),
        "confirm_escrow_deposit" to mapOf(
            Language.AR to "تأكيد الدفع الائتماني وقفل الضمان 🔐",
            Language.EN to "Confirm Deposit & Lock Escrow 🔐",
            Language.FR to "Confirmer le dépôt & Bloquer 🔐"
        ),
        "toast_success_escrow" to mapOf(
            Language.AR to "تم إيداع الأموال في محفظة الضمان بنجاح! رقم المعاملة قيد المعالجة.",
            Language.EN to "Successfully deposited and locked funds in Escrow! Transaction is pending.",
            Language.FR to "Fonds déposés et bloqués avec succès ! Transaction en attente."
        ),
        "toast_fill_fields" to mapOf(
            Language.AR to "يرجى ملء جميع الحقول المطلوبة بشكل صحيح!",
            Language.EN to "Please fill all required fields correctly!",
            Language.FR to "Veuillez remplir correctement tous les champs requis !"
        ),
        "tab_browse" to mapOf(
            Language.AR to "تصفح الأغنام 🐏",
            Language.EN to "Browse Rams 🐏",
            Language.FR to "Découvrir 🐏"
        ),
        "tab_buyer_dashboard" to mapOf(
            Language.AR to "تداولاتي (مشتري) 🛒",
            Language.EN to "My Orders (Buyer) 🛒",
            Language.FR to "Mes Achats 🛒"
        ),
        "tab_farmer_space" to mapOf(
            Language.AR to "أدوات المربي 🌿",
            Language.EN to "Farmer Space 🌿",
            Language.FR to "Espace Éleveur 🌿"
        ),
        "tab_disputes" to mapOf(
            Language.AR to "مركز النزاعات (وسيط) ⚖️",
            Language.EN to "Disputes (Mediator) ⚖️",
            Language.FR to "Litiges (Médiateur) ⚖️"
        ),
        "status_held" to mapOf(
            Language.AR to "محجوز بالضمان 🔐",
            Language.EN to "HELD IN ESCROW 🔐",
            Language.FR to "SÉQUESTRÉ (ESCROW) 🔐"
        ),
        "status_released" to mapOf(
            Language.AR to "تم الصرف للمربي 💸",
            Language.EN to "RELEASED TO FARMER 💸",
            Language.FR to "LIBÉRÉ À L'ÉLEVEUR 💸"
        ),
        "status_refunded" to mapOf(
            Language.AR to "تم الإرجاع للمشتري ↩️",
            Language.EN to "REFUNDED TO BUYER ↩️",
            Language.FR to "REMBOURSÉ À L'ACHETEUR ↩️"
        ),
        "status_disputed" to mapOf(
            Language.AR to "قيد النزاع والتحكيم ⚠️",
            Language.EN to "UNDER DISPUTE ⚠️",
            Language.FR to "EN LITIGE / ARBITRAGE ⚠️"
        ),
        "seller_payout" to mapOf(
            Language.AR to "قيمة الضمان قيد التحويل:",
            Language.EN to "Escrow Funds value:",
            Language.FR to "Valeur des fonds d'escrow :"
        ),
        "action_confirm_delivery" to mapOf(
            Language.AR to "تأكيد الاستلام وصرف الأموال للمربي 👍",
            Language.EN to "Confirm Delivery & Release Payout 👍",
            Language.FR to "Confirmer la livraison & Libérer 👍"
        ),
        "action_raise_dispute" to mapOf(
            Language.AR to "فتح نزاع رسمي للتحكيم 🚨",
            Language.EN to "Raise Formal Dispute / Arbitrate 🚨",
            Language.FR to "Ouvrir un litige d'arbitrage 🚨"
        ),
        "no_transactions" to mapOf(
            Language.AR to "لا توجد أي تداولات جارية حالياً باسمك.",
            Language.EN to "No pending escrow transactions found in database.",
            Language.FR to "Aucune transaction sous escrow trouvée."
        ),
        "dispute_modal_title" to mapOf(
            Language.AR to "تقديم طلب تحكيم إلى الوسيط",
            Language.EN to "Submit Arbitration Ticket to Mediator",
            Language.FR to "Soumettre un dossier d'arbitrage"
        ),
        "dispute_reason" to mapOf(
            Language.AR to "سبب النزاع الأساسي:",
            Language.EN to "Primary Dispute Reason:",
            Language.FR to "Motif principal du litige :"
        ),
        "reason_weight" to mapOf(
            Language.AR to "اختلاف في الوزن الفعلي (Weight Discrepancy)",
            Language.EN to "Weight Discrepancy (Actual < Certified)",
            Language.FR to "Écart de poids (Réel < Certifié)"
        ),
        "reason_health" to mapOf(
            Language.AR to "مشاكل صحية أو عيوب شرعية (Health Issues)",
            Language.EN to "Health Issues / Veterinary Defects",
            Language.FR to "Problèmes de santé ou défauts vétérinaires"
        ),
        "reason_delay" to mapOf(
            Language.AR to "تأخر أو عدم التوصيل (Delivery Delay)",
            Language.EN to "Delivery Delay / Non-arrival",
            Language.FR to "Retard de livraison ou non-réception"
        ),
        "dispute_desc" to mapOf(
            Language.AR to "اشرح التفاصيل والمشكلة بالتفصيل للوسيط:",
            Language.EN to "Provide detailed description & proof for the mediator:",
            Language.FR to "Détails explicatifs et preuves pour le médiateur :"
        ),
        "desc_hint" to mapOf(
            Language.AR to "مثال: الوزن الفعلي كغ 60 بدلا من 80 كغ المذكورة، أو الأضحية تعاني من عرج واضح...",
            Language.EN to "Example: Actual weight is 60kg instead of 80kg, or sheep suffers visible health defects...",
            Language.FR to "Exemple : Poids réel 60kg au lieu de 80kg, ou boiterie visible chez l'animal..."
        ),
        "submit_dispute" to mapOf(
            Language.AR to "إرسال الشكوى وقفل الحساب مؤقتاً 🔒",
            Language.EN to "Lock Funds & Submit Dispute 🔒",
            Language.FR to "Bloquer les fonds & Soumettre 🔒"
        ),
        "mediator_panel_title" to mapOf(
            Language.AR to "محكمة الضمان الرقمية (لوحة تحكيم الوسيط)",
            Language.EN to "Digital Escrow Court (Mediator Arbitration)",
            Language.FR to "Cour d'Escrow Numérique (Arbitrage et Décisions)"
        ),
        "no_disputes" to mapOf(
            Language.AR to "السوق آمن ومستقر! لا توجد نزاعات مفتوحة للتحكيم حالياً.",
            Language.EN to "Exemplary market state! No active disputes require arbitration.",
            Language.FR to "Marché sain ! Aucun litige actif en attente de décision."
        ),
        "dispute_ticket" to mapOf(
            Language.AR to "تذكرة نزاع رقم #",
            Language.EN to "Dispute Ticket #",
            Language.FR to "Ticket de Litige n°"
        ),
        "dispute_by" to mapOf(
            Language.AR to "مقدم النزاع:",
            Language.EN to "Initiated By:",
            Language.FR to "Initié par :"
        ),
        "dispute_reason_label" to mapOf(
            Language.AR to "طبيعة الخلاف:",
            Language.EN to "Dispute Nature:",
            Language.FR to "Nature du différend :"
        ),
        "arbitrate_action" to mapOf(
            Language.AR to "البدء بالتحكيم واتخاذ القرار ⚖️",
            Language.EN to "Arbitrate & Clear Funds ⚖️",
            Language.FR to "Arbitrer & Statuer la somme ⚖️"
        ),
        "refund_buyer_button" to mapOf(
            Language.AR to "رد الأموال بالكامل للمشتري (Refund Buyer) ↩️",
            Language.EN to "Issue Complete Refund to Buyer ↩️",
            Language.FR to "Rembourser totalement l'acheteur ↩️"
        ),
        "release_farmer_button" to mapOf(
            Language.AR to "صرف الأرباح للمربي (Release to Farmer) 💸",
            Language.EN to "Release Full Payout to Farmer 💸",
            Language.FR to "Libérer le paiement à l'éleveur 💸"
        ),
        "toast_arbitrated" to mapOf(
            Language.AR to "تم التحكيم بنجاح وإقفال ملف النزاع وتسوية الأموال.",
            Language.EN to "Arbitration complete! Dispute resolved and funds successfully settled.",
            Language.FR to "Arbitrage terminé ! Conflit résolu et fonds réglés."
        ),
        "farmer_add_listing" to mapOf(
            Language.AR to "عرض أضحية جديدة للبيع وآمن للضمان الثلاثي",
            Language.EN to "Publish New Ram to Triple Escrow System",
            Language.FR to "Publier un bélier sur le marché sécurisé"
        ),
        "breed_label" to mapOf(
            Language.AR to "السلالة والنوع (Breed):",
            Language.EN to "Sheep Breed:",
            Language.FR to "Race du bélier :"
        ),
        "weight_input" to mapOf(
            Language.AR to "الوزن الدقيق بالكيلو غرام (كغ):",
            Language.EN to "Certified Exact Weight (kg):",
            Language.FR to "Poids exact certifié (kg) :"
        ),
        "location_input" to mapOf(
            Language.AR to "المنطقة / المدينة للتواجد:",
            Language.EN to "Farm Location / City:",
            Language.FR to "Localisation de l'élevage (Ville) :"
        ),
        "price_input" to mapOf(
            Language.AR to "السعر المطلوب (درهم مغربي):",
            Language.EN to "Demanded Price (DH):",
            Language.FR to "Prix demandé (DH) :"
        ),
        "farm_name" to mapOf(
            Language.AR to "اسم المزرعة أو المربي البائع:",
            Language.EN to "Farm or Registered Seller Name:",
            Language.FR to "Nom de l'élevage ou vendeur :"
        ),
        "vet_certification" to mapOf(
            Language.AR to "الفحص والتوثيق الطبي والصوري:",
            Language.EN to "Medical & Photographic Vet Certification:",
            Language.FR to "Certification photo & examen vétérinaire :"
        ),
        "sim_capture" to mapOf(
            Language.AR to "التقاط صورة طبية مبرهنة 📸",
            Language.EN to "Capture Verified Vet Photo 📸",
            Language.FR to "Capturer photo médicale 📸"
        ),
        "photo_certified" to mapOf(
            Language.AR to "تم التحقق: الصورة موثقة بيطرياً ومؤمنة رقمياً بسلسلة الضمان",
            Language.EN to "Verified: Photo medically certified and cryptographic block registered ✅",
            Language.FR to "Vérifié : Photo certifiée et insérée au registre d'escrow ✅"
        ),
        "ear_tag_label" to mapOf(
            Language.AR to "رقم حلقة الترقيم الوطنية (ONSSA Ear Tag):",
            Language.EN to "ONSSA National Ear Tag Ring Number:",
            Language.FR to "Code bague d'identification nationale (ONSSA) :"
        ),
        "ear_tag_placeholder" to mapOf(
            Language.AR to "مثال: ONSSA-SRD-2026-8874",
            Language.EN to "e.g. ONSSA-SRD-2026-8874",
            Language.FR to "ex: ONSSA-SRD-2026-8874"
        ),
        "camera_man_title" to mapOf(
            Language.AR to "محاكاة عدسة الكاميرا البيطرية النشطة 📸",
            Language.EN to "Active Vet Camera Shutter Viewfinder 📸",
            Language.FR to "Viseur actif de la caméra vétérinaire 📸"
        ),
        "camera_tap_to_capture" to mapOf(
            Language.AR to "ثبّت الكاميرا على جبهة الكبش أو رقم الحلقة ثم اضغط زر الالتقاط الدائري بالأسفل:",
            Language.EN to "Align the viewfinder with the ram's face or ear tag, then tap the red shutter below:",
            Language.FR to "Alignez le viseur avec la tête du bélier ou le code bague, puis appuyez sur le bouton rouge :"
        ),
        "camera_click_shutter" to mapOf(
            Language.AR to "التقاط الصورة الطبية 🔴",
            Language.EN to "Manual Shutter Capture 🔴",
            Language.FR to "Déclenchement Manuel 🔴"
        ),
        "camera_retake" to mapOf(
            Language.AR to "تغيير الصورة وإعادة الالتقاط 🔄",
            Language.EN to "Retake & Clear Photo 🔄",
            Language.FR to "Prendre une autre photo 🔄"
        ),
        "publish_listing" to mapOf(
            Language.AR to "طرح الأضحية في السوق الرقمي 🐏",
            Language.EN to "Publish Sheep Listing Now 🐏",
            Language.FR to "Mettre le bélier en vente 🐏"
        ),
        "toast_listing_published" to mapOf(
            Language.AR to "تم نشر أضحيتك بنجاح! يمكن للمشترين الآن حجزها بالضمان.",
            Language.EN to "Listing published successfully! Buyers can now lock funds in escrow.",
            Language.FR to "Bélier publié ! L'acheteur peut désormais bloquer les fonds."
        ),
        "current_role_active" to mapOf(
            Language.AR to "الهوية النشطة حالياً:",
            Language.EN to "Active Role Persona:",
            Language.FR to "Rôle actif actuel :"
        ),
        "back_to_onboarding" to mapOf(
            Language.AR to "تغيير هوية التداول 🔄",
            Language.EN to "Switch Trader Identity 🔄",
            Language.FR to "Changer de rôle 🔄"
        ),
        "spec_dialog_title" to mapOf(
            Language.AR to "مواصفات تداول حولي مضمون 🔐",
            Language.EN to "Hawli Madmon Security Protocol Spec 🔐",
            Language.FR to "Spécification de Sécurité Hawli Madmon 🔐"
        ),
        "spec_paragraph" to mapOf(
            Language.AR to "تعتمد منصة حولي مضمون على بروتوكول أمان للتداول الثلاثي يتمحور حول النزاهة والتحقق من الوزن والصحة قبل تحرير المدفوعات:\n\n• محفظة الضمان (Escrow): يتم قفل ثمن الأضحية بالكامل ولا يحق للمربي سحبه ولا يمكن للمشتري استعادته بمجرد الموافقة وتعبئة الرصيد.\n• قاعدة بيانات Room: تستخدم كمستودع متزامن محلي لعلاقات الأغنام، والمبيعات والنزاعات.\n• الفحص الطبي الصوري (Vet Validation): يمنع عرض أي ماشية في السوق بدون إقرار سلامتها الطبية والتقاط صورة مشفرة.\n• نظام التحكيم (Mediator Rule): المصلح يمتلك المفاتيح الفيدرالية لحل التضارب بناء على الفواتير المعتمدة ليعود الحق لأهله بأمان مطلق.",
            Language.EN to "Hawli Madmon operates on a strict Tripartite security protocol insuring compliance in weight and safety before payouts are unlocked:\n\n• Smart Hold (Escrow): Money is locked in state-driven intermediate storage. Neither the buyer can recall arbitrarily nor the seller cash-out without validation.\n• Room DB Local Arch: Secure SQL storage capturing items, transactional logs, and dispute states in offline-sync structure.\n• Veterinary Verification: High-fidelity image telemetry binds animal health profiles with cryptographic IDs.\n• Mediator Resolution: Independent arbitration keys settle conflicts cleanly based on physical delivery and medical receipts.",
            Language.FR to "Hawli Madmon est basé sur un protocole tripartite strict assurant la conformité du poids et de la santé animale avant déblocage :\n\n• Séquestre Intelligent (Escrow) : Fonds verrouillés. Aucun retrait unilatéral possible de l'acheteur ou éleveur.\n• Room DB : Stockage local SQL gérant les béliers, transactions et litiges.\n• Certification Vétérinaire : Indexation d'images médicales aux identifiants cryptographiques.\n• Arbitrage : Médiation neutre pour résoudre les litiges d'après les preuves fournies."
        ),
        "close" to mapOf(
            Language.AR to "إغلاق نافذة التوضيح",
            Language.EN to "Close Protocol Details",
            Language.FR to "Fermer les spécifications"
        ),
        "login_tab" to mapOf(
            Language.AR to "تسجيل الدخول",
            Language.EN to "Log In",
            Language.FR to "Connexion"
        ),
        "signup_tab" to mapOf(
            Language.AR to "إنشاء حساب جديد",
            Language.EN to "Register",
            Language.FR to "S'inscrire"
        ),
        "username_lbl" to mapOf(
            Language.AR to "اسم المستخدم / المعرّف:",
            Language.EN to "Username:",
            Language.FR to "Nom d'utilisateur :"
        ),
        "password_lbl" to mapOf(
            Language.AR to "كلمة المرور الأمنية:",
            Language.EN to "Security Password:",
            Language.FR to "Mot de passe :"
        ),
        "fullname_lbl" to mapOf(
            Language.AR to "الاسم الكامل للمربي أو المشتري:",
            Language.EN to "Full Name (Legal Profile):",
            Language.FR to "Nom complet :"
        ),
        "phone_lbl" to mapOf(
            Language.AR to "رقم الهاتف للتواصل (المغرب):",
            Language.EN to "Contact Phone (Morocco):",
            Language.FR to "Numéro de téléphone :"
        ),
        "form_step_title" to mapOf(
            Language.AR to "الخطوة الأخيرة: استمارة نوع الحساب 📋",
            Language.EN to "Final Step: Determine Account Type 📋",
            Language.FR to "Dernière étape : Choisir le type de compte 📋"
        ),
        "form_step_desc" to mapOf(
            Language.AR to "أهلاً بك! لتخصيص لوحة التحكم والتحقق بالشكل الصحيح، يرجى تحديد فئة نشاطك الأساسية على منصة حولي مضمون الآمنة للماشية والأضاحي:",
            Language.EN to "Welcome! To custom tailor your workspace, please select your transaction category on Hawli Madmon secure livestock exchange:",
            Language.FR to "Bienvenue ! Veuillez choisir votre rôle d'activité sur Hawli Madmon pour configurer votre tableau de bord sécurisé :"
        ),
        "form_terms" to mapOf(
            Language.AR to "أقر بوجوب الفحص الطبي الصادر والتوثيق والتحقق من الوزن ومطابقة شروط النزاهة تحت رعاية المصلح.",
            Language.EN to "I agree to weight checks, health cert compliance and honest tripartite arbitration guidelines.",
            Language.FR to "Je m'engage à respecter la conformité des poids, de la santé animale et l'arbitrage tripartite."
        ),
        "form_confirm_btn" to mapOf(
            Language.AR to "تفعيل وإنشاء الهوية النشطة 🚀",
            Language.EN to "Lock & Activate Identity 🚀",
            Language.FR to "Activer l'identité du compte 🚀"
        ),
        "select_role_err" to mapOf(
            Language.AR to "يرجى تحديد دور واحد من الفئات الثلاث للمتابعة.",
            Language.EN to "Please select one of the three roles to proceed.",
            Language.FR to "Veuillez choisir un rôle pour continuer."
        ),
        "accept_terms_err" to mapOf(
            Language.AR to "يرجى قراءة التعهد والموافقة عليه لتفعيل الحساب.",
            Language.EN to "You must accept the safety terms before activation.",
            Language.FR to "Veuillez accepter l'engagement pour continuer."
        )
    )

    fun get(key: String, language: Language): String {
        return data[key]?.get(language) ?: "[$key]"
    }
}
