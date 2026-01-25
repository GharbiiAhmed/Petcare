package tn.petcare_android.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tn.petcare_android.NotificationNavData
import tn.petcare_android.data.api.RetrofitInstance
import tn.petcare_android.data.repository.SubscriptionRepository
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.ui.components.NavigationDrawerOverlay
import tn.petcare_android.ui.screens.home.HomeScreen
import tn.petcare_android.ui.screens.profile.ProfileScreen
import tn.petcare_android.ui.screens.discover.DiscoverScreen
import tn.petcare_android.ui.screens.calendar.CalendarScreen
import tn.petcare_android.ui.screens.join.JoinScreen
import tn.petcare_android.ui.screens.chat.ChatAIScreen
import tn.petcare_android.ui.screens.petsitter.PetSitterScreen
import tn.petcare_android.ui.screens.petsitter.AvailableSittersScreen
import tn.petcare_android.ui.screens.vet.FindVetScreen
import tn.petcare_android.ui.screens.medical.MedicalHistoryScreen
import tn.petcare_android.ui.screens.medical.MedicalScreen
import tn.petcare_android.ui.screens.profile.EditProfileScreen
import tn.petcare_android.ui.screens.help.HelpScreen
import tn.petcare_android.ui.screens.map.MapScreen
import tn.petcare_android.ui.screens.chat.ConversationsListScreen
import tn.petcare_android.ui.screens.chat.ChatViewScreen
import tn.petcare_android.ui.screens.auth.VerifyEmailScreen
import tn.petcare_android.ui.screens.booking.BookingCreateScreen
import tn.petcare_android.ui.screens.booking.BookingDetailScreen
import tn.petcare_android.ui.screens.booking.BookingListScreen
import tn.petcare_android.ui.screens.booking.BookingUpdateScreen
import tn.petcare_android.ui.screens.calendar.AddCalendarEventScreen
import tn.petcare_android.ui.screens.calendar.PetCalendarScreen
import tn.petcare_android.ui.screens.chat.ChatAIScreen
import tn.petcare_android.ui.screens.findhub.FindHubScreen
import tn.petcare_android.ui.screens.join.JoinPetSitterScreen
import tn.petcare_android.ui.screens.join.JoinSalonScreen
import tn.petcare_android.ui.screens.join.JoinTeamScreen
import tn.petcare_android.ui.screens.join.JoinVetScreen
import tn.petcare_android.ui.screens.mypets.MyPetsScreen
import tn.petcare_android.ui.screens.notification.NotificationsScreen
import tn.petcare_android.ui.screens.pet.AddPetFlowScreen
import tn.petcare_android.ui.screens.pet.EditPetScreen
import tn.petcare_android.ui.screens.petdetail.PetProfileScreen
import tn.petcare_android.ui.screens.petsitter.PetSitterProfileScreen
import tn.petcare_android.ui.screens.subscription.EmailVerificationScreen
import tn.petcare_android.ui.screens.subscription.JoinWithSubscriptionScreen
import tn.petcare_android.ui.screens.subscription.SubscriptionManagementScreen
import tn.petcare_android.ui.screens.vet.VetProfileScreen
import tn.petcare_android.util.JwtDecoder
import tn.petcare_android.viewmodel.auth.AuthViewModel
import tn.petcare_android.viewmodel.auth.AuthViewModelFactory
import tn.petcare_android.viewmodel.booking.BookingViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileViewModel
import tn.petcare_android.viewmodel.profile.ProfileViewModelFactory
import tn.petcare_android.viewmodel.profile.ProfileUiState
import tn.petcare_android.util.ProfileCompletionUtil
import tn.petcare_android.util.SubscriptionManager
import tn.petcare_android.viewmodel.booking.BookingViewModel
import tn.petcare_android.viewmodel.chat.ChatViewModel
import tn.petcare_android.ui.screens.trainers.TrainersListScreen
import tn.petcare_android.ui.screens.trainers.TrainerDetailScreen
import tn.petcare_android.ui.screens.marketplace.MarketplaceListingsScreen
import tn.petcare_android.ui.screens.marketplace.MarketplaceDetailScreen
import tn.petcare_android.ui.screens.adoption.AdoptionListingsScreen
import tn.petcare_android.ui.screens.adoption.AdoptionDetailScreen
import tn.petcare_android.ui.screens.salon.FindSalonScreen
import tn.petcare_android.ui.screens.profile.SalonProfileScreen

@Composable
fun MainScreen(
    themePreference: ThemePreference,
    notificationNavData: NotificationNavData? = null,
    onNotificationHandled: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Create AuthViewModel for verification screen
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )
    
    // Profile ViewModel for completion check
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val profileUiState by profileViewModel.uiState.collectAsState()
    
    // Profile completion state
    var showProfileCompletionAlert by remember { mutableStateOf(false) }
    var shouldPresentEditProfile by remember { mutableStateOf(false) }
    
    // Check profile completion
    LaunchedEffect(profileUiState) {
        if (profileUiState is ProfileUiState.Success) {
            val user = (profileUiState as ProfileUiState.Success).user
            if (ProfileCompletionUtil.requiresProfileCompletion(user)) {
                showProfileCompletionAlert = true
            }
        }
    }
    
    // Handle profile completion alert
    if (showProfileCompletionAlert && profileUiState is ProfileUiState.Success) {
        val user = (profileUiState as ProfileUiState.Success).user
        AlertDialog(
            onDismissRequest = { showProfileCompletionAlert = false },
            title = { Text("Complete your profile to start") },
            text = { Text(ProfileCompletionUtil.getMissingFieldsMessage(user)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProfileCompletionAlert = false
                        // Navigate directly to edit profile screen (iOS Reference: MainTabView.swift line 77)
                        navController.navigate("edit_profile")
                    }
                ) {
                    Text("Complete Now")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProfileCompletionAlert = false }
                ) {
                    Text("Later")
                }
            }
        )
    }
    
    // Auto-navigate to edit profile if needed
    LaunchedEffect(shouldPresentEditProfile) {
        if (shouldPresentEditProfile) {
            delay(100)
            navController.navigate("edit_profile")
            shouldPresentEditProfile = false
        }
    }
    
    // Initialize SubscriptionManager (iOS Reference: MainTabView.swift line 11)
    LaunchedEffect(Unit) {
        val tokenManager = TokenManager(context)
        val repository = SubscriptionRepository(
            RetrofitInstance.subscriptionApi
        )
        SubscriptionManager.initialize(repository, tokenManager)
        // Check subscription status on start (iOS Reference: MainTabView.swift line 127)
        SubscriptionManager.checkSubscriptionStatus()
    }
    
    // Show expiration alert if needed (iOS Reference: SubscriptionManager.swift line 14)
    val showExpirationAlert by SubscriptionManager.showExpirationAlert.collectAsState()
    val expirationMessage by SubscriptionManager.expirationMessage.collectAsState()
    
    // Handle notification navigation
    LaunchedEffect(notificationNavData) {
        notificationNavData?.let { data ->
            when (data.destination) {
                "chat" -> {
                    // Navigate to conversations or specific conversation
                    if (data.conversationId != null) {
                        navController.navigate("conversation/${data.conversationId}") {
                            popUpTo("home")
                        }
                    } else {
                        navController.navigate("conversations") {
                            popUpTo("home")
                        }
                    }
                }
                "booking_detail" -> {
                    // Navigate to booking detail
                    data.bookingId?.let { bookingId ->
                        navController.navigate("booking_detail/$bookingId") {
                            popUpTo("home")
                        }
                    }
                }
                "notifications" -> {
                    navController.navigate("notifications") {
                        popUpTo("home")
                    }
                }
            }
            onNotificationHandled()
        }
    }
    
    // Expiration alert dialog (iOS Reference: SubscriptionManager.swift line 64)
    if (showExpirationAlert && expirationMessage != null) {
        AlertDialog(
            onDismissRequest = {
                SubscriptionManager.dismissExpirationAlert()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        "Warning",
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subscription Alert")
                }
            },
            text = { Text(expirationMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        SubscriptionManager.dismissExpirationAlert()
                        // Navigate to subscription management if needed
                        navController.navigate("subscription_management")
                    }
                ) {
                    Text("View Subscription")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        SubscriptionManager.dismissExpirationAlert()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Navigation is now handled through top drawer menu instead of bottom bar
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Unspecified // Unspecified allows paw print background to show through
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home", // Match iOS default tab
                modifier = Modifier.padding(innerPadding)
            ) {
            // iOS tab: Home
            composable("home") {
                HomeScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // iOS tab: Discover
            composable("discover") {
                DiscoverScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("chat_ai") {
              ChatAIScreen(
                  navController = navController,


              )
            }

            composable("mypets") {
                MyPetsScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("community") {
                tn.petcare_android.ui.screens.community.CommunityScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }
            
            // User Profile from Community
            composable(
                route = "user_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                tn.petcare_android.ui.screens.community.UserProfileScreen(
                    navController = navController,
                    userId = userId
                )
            }

                    composable("profile") {
                        ProfileScreen(
                            navController = navController,
                            onNavigateToChangePassword = onNavigateToChangePassword,
                            onNavigateToChangeEmail = onNavigateToChangeEmail,
                            onLogout = onLogout
                        )
                    }

            // Additional screens
            composable("join") {
                JoinTeamScreen(
                    navController = navController
                )
            }
            
            composable("join_team") {
                JoinTeamScreen(
                    navController = navController
                )
            }

            composable("join_vet") {
                JoinVetScreen(
                    navController = navController,
                    themePreference = themePreference,
                    fromSubscription = true // Always true when coming from subscription flow
                )
            }

            composable("join_sitter") {
                JoinPetSitterScreen(
                    navController = navController,
                    themePreference = themePreference,
                    fromSubscription = true // Always true when coming from subscription flow
                )
            }
            
            composable("join_trainer") {
                // TODO: Create JoinTrainerScreen similar to JoinVetScreen and JoinPetSitterScreen
                // For now, navigate to profile screen where trainer can set up their profile
                ProfileScreen(
                    navController = navController,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onNavigateToChangeEmail = onNavigateToChangeEmail,
                    onLogout = onLogout
                )
            }
            
            composable("join_salon") {
                tn.petcare_android.ui.screens.join.JoinSalonScreen(
                    navController = navController,
                    themePreference = themePreference,
                    fromSubscription = true
                )
            }
            
            composable("edit_vet_profile") {
                tn.petcare_android.ui.screens.profile.EditVetProfileScreen(
                    navController = navController
                )
            }
            
            composable("edit_sitter_profile") {
                tn.petcare_android.ui.screens.profile.EditSitterProfileScreen(
                    navController = navController
                )
            }

            composable("add_pet") {
                AddPetFlowScreen(navController = navController)
            }

            composable("calendar") {
                CalendarScreen(navController = navController)
            }
            
            // Pet Calendar Screen - Shows calendar for a specific pet
            composable("calendar/{petId}",
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                PetCalendarScreen(
                    navController = navController,
                    petId = petId
                )
            }
            
            // Add Calendar Event - Manual reminder creation (iOS Reference: AddCalendarEventView.swift)
            composable("add_calendar_event") { backStackEntry ->
                // Extract query parameters from savedStateHandle (set via navigate with arguments)
                val petId = backStackEntry.savedStateHandle.get<String>("petId")
                val eventType = backStackEntry.savedStateHandle.get<String>("type")

                AddCalendarEventScreen(
                    navController = navController,
                    themePreference = themePreference,
                    petId = petId,
                    eventType = eventType
                )
            }
            
            // Find Hub - Unified navigation to care services (iOS Reference: FindHubView.swift)
            composable("find_hub") {
                FindHubScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("petsitter") {
                PetSitterScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // Available Sitters Screen (iOS Reference: AvailableSittersView.swift)
            composable("available_sitters") {
                AvailableSittersScreen(
                    navController = navController
                )
            }

            composable("clinic") {
                FindVetScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // Trainers
            composable("trainers_list") {
                TrainersListScreen(
                    navController = navController
                )
            }

            composable("trainer_detail/{trainerId}") { backStackEntry ->
                val trainerId = backStackEntry.arguments?.getString("trainerId")
                TrainerDetailScreen(
                    navController = navController,
                    trainerId = trainerId
                )
            }

            // Salons
            composable("salons") {
                tn.petcare_android.ui.screens.salon.FindSalonScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("salon_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                SalonProfileScreen(
                    navController = navController,
                    onNavigateToChangePassword = {
                        navController.navigate("change_password")
                    },
                    onNavigateToChangeEmail = {
                        navController.navigate("change_email")
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    salonId = userId
                )
            }

            // Marketplace
            composable("marketplace_listings") {
                MarketplaceListingsScreen(
                    navController = navController
                )
            }

            composable("marketplace_detail/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")
                MarketplaceDetailScreen(
                    navController = navController,
                    listingId = listingId
                )
            }

            // Adoption
            composable("adoption_listings") {
                AdoptionListingsScreen(
                    navController = navController
                )
            }

            composable("adoption_detail/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")
                AdoptionDetailScreen(
                    navController = navController,
                    listingId = listingId
                )
            }

            composable("create_marketplace_listing") {
                tn.petcare_android.ui.screens.marketplace.CreateMarketplaceListingScreen(
                    navController = navController
                )
            }

            composable("create_adoption_listing") {
                tn.petcare_android.ui.screens.adoption.CreateAdoptionListingScreen(
                    navController = navController
                )
            }

            composable("pet_detail/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                PetProfileScreen(navController = navController, petId = petId)
            }

            composable("medical_history/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                MedicalHistoryScreen(navController = navController, petId = petId)
            }

            composable("medical") {
                MedicalScreen(navController = navController)
            }

            composable("edit_profile") {
                EditProfileScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable(
                route = "edit_pet/{petId}",
                arguments = listOf(navArgument("petId") { defaultValue = "" })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId").orEmpty()
                if (petId.isNotBlank()) {
                    EditPetScreen(
                        navController = navController,
                        petId = petId
                    )
                } else {
                    Text("Invalid pet ID")
                }
            }

            composable("help") {
                HelpScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("map") {
                MapScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("conversations") {
                ConversationsListScreen(
                    navController = navController,
                    themePreference = themePreference,
                    currentUserId = "current_user_id" // TODO: Get from session
                )
            }
            
            // Direct conversation navigation (from notifications)
            composable("conversation/{conversationId}") { backStackEntry ->
                val context = LocalContext.current
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                val chatViewModel: ChatViewModel = viewModel()
                val conversations by chatViewModel.conversations.collectAsState()
                
                // Get current user ID from token
                val tokenManager = remember { TokenManager(context) }
                val currentUserId = remember {
                    var userId = ""
                    runBlocking {
                        val token = tokenManager.getAccessToken().firstOrNull()
                        userId = if (token != null) {
                            JwtDecoder.getUserIdFromToken(token) ?: ""
                        } else {
                            ""
                        }
                    }
                    userId
                }
                
                // Find conversation to get recipient details
                LaunchedEffect(conversationId) {
                    if (conversations.isEmpty()) {
                        chatViewModel.loadConversations()
                    }
                }
                
                val conversation = conversations.find { it.normalizedId == conversationId }
                val recipient = conversation?.participants?.firstOrNull { it.normalizedId != currentUserId }
                
                ChatViewScreen(
                    navController = navController,
                    themePreference = themePreference,
                    recipientId = recipient?.normalizedId ?: "",
                    recipientName = recipient?.name ?: "Chat",
                    currentUserId = currentUserId,
                    conversationId = conversationId
                )
            }

            composable("chat/{recipientId}/{recipientName}") { backStackEntry ->
                val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
                val recipientName = backStackEntry.arguments?.getString("recipientName") ?: "User"
                val context = LocalContext.current
                val tokenManager = remember { TokenManager(context) }
                val currentUserId = remember {
                    var userId = ""
                    runBlocking {
                        val token = tokenManager.getAccessToken().firstOrNull()
                        userId = if (token != null) {
                            JwtDecoder.getUserIdFromToken(token) ?: ""
                        } else {
                            ""
                        }
                    }
                    userId
                }
                
                // iOS: Just pass recipientId/Name, ChatViewScreen handles getOrCreateConversation
                ChatViewScreen(
                    navController = navController,
                    themePreference = themePreference,
                    recipientId = recipientId,
                    recipientName = recipientName,
                    currentUserId = currentUserId,
                    conversationId = null // Will be created automatically if needed
                )
            }

            composable("vet_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                VetProfileScreen(
                    navController = navController,
                    vetId = userId
                )
            }

            composable("sitter_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PetSitterProfileScreen(
                    navController = navController,
                    sitterId = userId
                )
            }
            
            // Booking screens (match iOS)
            composable("booking_list") {
                val bookingVm: BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                BookingListScreen(
                    viewModel = bookingVm,
                    onBookingSelected = { booking -> navController.navigate("booking_detail/${booking.id}") },
                    navController = navController
                )
            }

            composable("booking_create") {
                val bookingVm: BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                BookingCreateScreen(
                    viewModel = bookingVm,
                    onBookingCreated = { navController.navigate("booking_list") }
                )
            }
            
            composable("booking_create/{providerId}/{providerName}/{providerType}") { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                val providerName = backStackEntry.arguments?.getString("providerName") ?: ""
                val providerType = backStackEntry.arguments?.getString("providerType") ?: "vet"
                val bookingVm: BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                BookingCreateScreen(
                    viewModel = bookingVm,
                    providerId = providerId,
                    providerName = providerName,
                    providerType = providerType,
                    onBookingCreated = {
                        navController.navigate("booking_list") {
                            popUpTo("booking_list") { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("booking_detail/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingVm: BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                LaunchedEffect(bookingId) { bookingVm.fetchBookingById(bookingId) }
                BookingDetailScreen(
                    booking = bookingVm.selectedBooking.collectAsState().value,
                    onBack = { navController.popBackStack() },
                    viewModel = bookingVm
                )
            }

            composable("booking_update/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingVm: BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                BookingUpdateScreen(
                    viewModel = bookingVm,
                    bookingId = bookingId,
                    onBookingUpdated = { navController.navigate("booking_detail/$bookingId") }
                )
            }
            
            // Notifications screen (iOS Reference: NotificationsView.swift)
            composable("notifications") {
                NotificationsScreen(
                    navController = navController,
                )
            }
            
            // Subscription Management (iOS Reference: STRIPE_SUBSCRIPTION_IMPLEMENTATION.md)
            composable("subscription_management") {
                SubscriptionManagementScreen(
                    navController = navController
                )
            }
            
            // New Subscription Flow
            composable("subscription_benefits") {
                tn.petcare_android.ui.screens.subscription.SubscriptionBenefitsScreen(
                    navController = navController
                )
            }
            
            // Old subscription flow - kept for backward compatibility
            composable("join_vet_sitter") {
                JoinWithSubscriptionScreen(
                    navController = navController
                )
            }
            
            composable("email_verification") {
                EmailVerificationScreen(
                    navController = navController
                )
            }
            
            // Email verification (from conversion flow)
            composable(
                route = "verify?email={email}",
                arguments = listOf(navArgument("email") { 
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerifyEmailScreen(
                    viewModel = authViewModel,
                    email = email,
                    onVerified = {
                        navController.popBackStack()
                    }
                )
            }
        }
        }
        
        // Sidebar drawer overlay with user info
        val userName = if (profileUiState is ProfileUiState.Success) {
            (profileUiState as ProfileUiState.Success).user.name
        } else null
        
        val userImage = if (profileUiState is ProfileUiState.Success) {
            (profileUiState as ProfileUiState.Success).user.profileImage
        } else null

        NavigationDrawerOverlay(
            navController = navController,
            userName = userName,
            userImage = userImage
        )
    }
}

