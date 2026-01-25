package tn.petcare_android.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.firstOrNull
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import tn.petcare_android.data.storage.ThemePreference
import tn.petcare_android.data.storage.TokenManager
import tn.petcare_android.data.session.SessionManager
import tn.petcare_android.ui.components.SplashScreen
import tn.petcare_android.ui.screens.auth.LoginScreen
import tn.petcare_android.ui.screens.auth.RegisterScreen
import tn.petcare_android.ui.screens.auth.VerifyEmailScreen
import tn.petcare_android.ui.screens.auth.ForgotPasswordScreen
import tn.petcare_android.ui.screens.auth.ResetPasswordScreen
import tn.petcare_android.ui.screens.settings.ChangePasswordScreen
import tn.petcare_android.ui.screens.settings.ChangeEmailScreen
import tn.petcare_android.ui.screens.settings.VerifyNewEmailScreen
import tn.petcare_android.ui.screens.role.AdminMainScreen
import tn.petcare_android.ui.screens.role.VetMainScreen
import tn.petcare_android.ui.screens.role.TrainerMainScreen
import tn.petcare_android.ui.screens.role.PetSitterMainScreen
import tn.petcare_android.ui.screens.role.SalonMainScreen
import tn.petcare_android.ui.screens.profile.AdminProfileScreen
import tn.petcare_android.ui.screens.profile.VetProfileScreen
import tn.petcare_android.ui.screens.profile.SitterProfileScreen
import tn.petcare_android.ui.screens.profile.TrainerProfileScreen
import tn.petcare_android.ui.screens.profile.SalonProfileScreen
import tn.petcare_android.ui.screens.profile.EditVetProfileScreen
import tn.petcare_android.ui.screens.profile.EditSitterProfileScreen
import tn.petcare_android.ui.screens.calendar.CalendarScreen
import tn.petcare_android.ui.screens.calendar.AddCalendarEventScreen
import tn.petcare_android.ui.screens.trainers.TrainerDetailScreen
import tn.petcare_android.ui.screens.petdetail.PetProfileScreen
import tn.petcare_android.ui.screens.chat.ConversationsListScreen
import tn.petcare_android.ui.screens.chat.ChatViewScreen
import tn.petcare_android.ui.screens.salon.FindSalonScreen
import tn.petcare_android.util.JwtDecoder
import kotlinx.coroutines.runBlocking
import tn.petcare_android.viewmodel.chat.ChatViewModel
import tn.petcare_android.viewmodel.auth.AuthViewModel
import tn.petcare_android.viewmodel.auth.AuthViewModelFactory
import tn.petcare_android.util.RoleNavigationUtil

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY = "verify"
    const val MAIN = "main"
    const val OWNER_MAIN = "owner_main"
    const val ADMIN_MAIN = "admin_main"
    const val ADMIN_PROFILE = "admin_profile"
    const val VET_MAIN = "vet_main"
    const val VET_PROFILE = "vet_profile"
    const val TRAINER_MAIN = "trainer_main"
    const val TRAINER_PROFILE = "trainer_profile"
    const val SITTER_MAIN = "sitter_main"
    const val SITTER_PROFILE = "sitter_profile"
    const val SALON_MAIN = "salon_main"
    const val SALON_PROFILE = "salon_profile"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val CHANGE_PASSWORD = "change_password"
    const val CHANGE_EMAIL = "change_email"
    const val VERIFY_NEW_EMAIL = "verify_new_email"
}

@Composable
fun AppNavGraph(
    context: Context,
    themePreference: ThemePreference,
    notificationNavData: tn.petcare_android.NotificationNavData? = null,
    onNotificationHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val factory = AuthViewModelFactory(context)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val sessionManager = remember { SessionManager.getInstance(context) }

    var startDestination by remember { mutableStateOf<String?>(null) }

    val tokenManager = remember { TokenManager(context) }

    LaunchedEffect(Unit) {
        //splash screen duration
        kotlinx.coroutines.delay(4500)

        // Restore session - this will automatically refresh tokens if needed
        val isAuthenticated = sessionManager.restoreSession()
        startDestination = if (isAuthenticated) {
            // Route based on user role
            RoleNavigationUtil.getRouteForRole(tokenManager)
        } else {
            Routes.LOGIN
        }
    }

    if (startDestination == null) {
        SplashScreen()
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!, modifier = modifier) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onNavigateToHome = {
                    // Route based on user role after login
                    val route = kotlinx.coroutines.runBlocking {
                        RoleNavigationUtil.getRouteForRole(tokenManager)
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToVerify = { email ->
                    navController.navigate("${Routes.VERIFY}?email=$email")
                },
                onNavigateToHome = {
                    // Route based on user role after registration
                    val route = kotlinx.coroutines.runBlocking {
                        RoleNavigationUtil.getRouteForRole(tokenManager)
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.VERIFY}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { backstackEntry ->
            val email = backstackEntry.arguments?.getString("email") ?: ""
            VerifyEmailScreen(
                viewModel = authViewModel,
                email = email,
                onVerified = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // Forgot Password Flow
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateToResetPassword = { email ->
                    navController.navigate("${Routes.RESET_PASSWORD}?email=$email")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.RESET_PASSWORD}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { backstackEntry ->
            val email = backstackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                viewModel = authViewModel,
                email = email,
                onPasswordReset = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Change Password (requires authentication)
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                viewModel = authViewModel,
                onPasswordChanged = {
                    // After password change, user is logged out, go to login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Change Email Flow (requires authentication)
        composable(Routes.CHANGE_EMAIL) {
            ChangeEmailScreen(
                viewModel = authViewModel,
                onEmailChanged = {
                    // Navigate to login after email change
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToVerify = { newEmail ->
                    navController.navigate("${Routes.VERIFY_NEW_EMAIL}?email=$newEmail")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.VERIFY_NEW_EMAIL}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { backstackEntry ->
            val email = backstackEntry.arguments?.getString("email") ?: ""
            VerifyNewEmailScreen(
                viewModel = authViewModel,
                newEmail = email,
                onEmailVerified = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Role-based main screens
        composable(Routes.OWNER_MAIN) {
            MainScreen(
                themePreference = themePreference,
                notificationNavData = notificationNavData,
                onNotificationHandled = onNotificationHandled,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Keep MAIN route for backward compatibility - redirect to owner_main
        composable(Routes.MAIN) {
            LaunchedEffect(Unit) {
                navController.navigate(Routes.OWNER_MAIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            }
        }

        // Admin Main Screen
        composable(Routes.ADMIN_MAIN) {
            AdminMainScreen(
                navController = navController,
                themePreference = themePreference,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Profile Screen
        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(
                navController = navController,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Vet Main Screen
        composable(Routes.VET_MAIN) {
            VetMainScreen(
                navController = navController,
                themePreference = themePreference,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Vet Profile Screen
        composable(Routes.VET_PROFILE) {
            VetProfileScreen(
                navController = navController,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Trainer Main Screen
        composable(Routes.TRAINER_MAIN) {
            TrainerMainScreen(
                navController = navController,
                themePreference = themePreference,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Trainer Profile Screen
        composable(Routes.TRAINER_PROFILE) {
            TrainerProfileScreen(
                navController = navController,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Pet Sitter Main Screen
        composable(Routes.SITTER_MAIN) {
            PetSitterMainScreen(
                navController = navController,
                themePreference = themePreference,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Sitter Profile Screen
        composable(Routes.SITTER_PROFILE) {
            SitterProfileScreen(
                navController = navController,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Salon Main Screen
        composable(Routes.SALON_MAIN) {
            SalonMainScreen(
                navController = navController,
                themePreference = themePreference,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Salon Profile Screen
        composable(Routes.SALON_PROFILE) {
            SalonProfileScreen(
                navController = navController,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Profile Edit Screens for Role-Specific Users
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

        composable("edit_salon_profile") {
            tn.petcare_android.ui.screens.profile.EditSalonProfileScreen(
                navController = navController
            )
        }

        // Calendar route for all roles
        composable("calendar") {
            tn.petcare_android.ui.screens.calendar.CalendarScreen(
                navController = navController
            )
        }

        // Add Calendar Event route
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

        composable("trainer_detail/{trainerId}",
            arguments = listOf(navArgument("trainerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trainerId = backStackEntry.arguments?.getString("trainerId")
            tn.petcare_android.ui.screens.trainers.TrainerDetailScreen(
                navController = navController,
                trainerId = trainerId
            )
        }

        // Conversations route for all roles
        composable("conversations") {
            val tokenManager = remember { TokenManager(context) }
            val currentUserId = remember {
                var userId = ""
                runBlocking {
                    val token: String? = tokenManager.getAccessToken().firstOrNull()
                    userId = token?.let { 
                        JwtDecoder.getUserIdFromToken(it) 
                    } ?: ""
                }
                userId
            }
            ConversationsListScreen(
                navController = navController,
                themePreference = themePreference,
                currentUserId = currentUserId
            )
        }

        // Conversation detail route
        composable("conversation/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val chatViewModel: tn.petcare_android.viewmodel.chat.ChatViewModel = viewModel()
            val conversations by chatViewModel.conversations.collectAsState()
            
            val tokenManager = remember { TokenManager(context) }
            val currentUserId = remember {
                var userId = ""
                runBlocking {
                    val token: String? = tokenManager.getAccessToken().firstOrNull()
                    userId = token?.let { 
                        JwtDecoder.getUserIdFromToken(it) 
                    } ?: ""
                }
                userId
            }
            
            LaunchedEffect(conversationId) {
                if (conversations.isEmpty()) {
                    chatViewModel.loadConversations()
                }
            }
            
            val conversation = conversations.find { conv -> conv.normalizedId == conversationId }
            val recipient = conversation?.participants?.firstOrNull { participant -> participant.normalizedId != currentUserId }
            
            ChatViewScreen(
                navController = navController,
                themePreference = themePreference,
                recipientId = recipient?.normalizedId ?: "",
                recipientName = recipient?.name ?: "Chat",
                currentUserId = currentUserId,
                conversationId = conversationId
            )
        }

        // Chat route
        composable("chat/{recipientId}/{recipientName}",
            arguments = listOf(
                navArgument("recipientId") { type = NavType.StringType },
                navArgument("recipientName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
            val recipientName = backStackEntry.arguments?.getString("recipientName") ?: "User"
            val tokenManager = remember { TokenManager(context) }
            val currentUserId = remember {
                var userId = ""
                runBlocking {
                    val token: String? = tokenManager.getAccessToken().firstOrNull()
                    userId = token?.let { 
                        JwtDecoder.getUserIdFromToken(it) 
                    } ?: ""
                }
                userId
            }
            
            ChatViewScreen(
                navController = navController,
                themePreference = themePreference,
                recipientId = recipientId,
                recipientName = recipientName,
                currentUserId = currentUserId,
                conversationId = null
            )
        }

        // Pet detail route
        composable("pet_detail/{petId}",
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            PetProfileScreen(navController = navController, petId = petId)
        }

        // Salons route
        composable("salons") {
            FindSalonScreen(
                navController = navController,
                themePreference = themePreference
            )
        }

        // Salon profile route with userId
        composable("salon_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SalonProfileScreen(
                navController = navController,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                salonId = userId
            )
        }
    }
}
