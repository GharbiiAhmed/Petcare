package tn.petcare_android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import android.util.Log
import coil.compose.rememberAsyncImagePainter
import tn.petcare_android.ui.theme.*


// Global state for drawer
object DrawerState {
    val isExpanded = mutableStateOf(false)
}

// Shared MenuItem data class for all drawer menus
data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun TopNavBar(
    title: String,
    navController: NavHostController? = null,
    showBackButton: Boolean = true,
    showMenuButton: Boolean = false,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    onBackClick: (() -> Unit)? = null,
    onMessagesClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    messageCount: Int = 0,
    notificationCount: Int = 0,
    actions: @Composable RowScope.() -> Unit = {},
    fontSize: TextUnit = 22.sp
) {
    Column(
        modifier = Modifier.background(HeaderBackground)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                when {
                    showBackButton -> {
                        TopNavIconButton(
                            icon = backIcon,
                            contentDescription = "Back",
                            onClick = {
                                if (onBackClick != null) {
                                    onBackClick()
                                } else {
                                    navController?.popBackStack()
                                }
                            }
                        )
                    }
                    showMenuButton -> {
                        TopNavIconButton(
                            icon = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            onClick = {
                                Log.d("TopNavBar", "Menu button clicked")
                                DrawerState.isExpanded.value = !DrawerState.isExpanded.value
                                Log.d("TopNavBar", "Drawer state: ${DrawerState.isExpanded.value}")
                            }
                        )
                    }
                    else -> {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()

                    onMessagesClick?.let {
                        TopNavBadgeButton(
                            icon = Icons.Filled.Email,
                            contentDescription = "Messages",
                            badgeCount = messageCount,
                            onClick = it
                        )
                    }

                    onNotificationsClick?.let {
                        TopNavBadgeButton(
                            icon = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            badgeCount = notificationCount,
                            onClick = it
                        )
                    }

                    onSettingsClick?.let {
                        TopNavIconButton(
                            icon = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            onClick = {
                                Log.d("TopNavBar", "Settings button clicked")
                                it()
                            }
                        )
                    }
                }
            }

        HorizontalDivider(
            thickness = 1.dp,
            color = VetStroke.copy(alpha = 0.5f)
        )
    }
}

// Sidebar Drawer Overlay Component
@Composable
fun NavigationDrawerOverlay(
    navController: NavHostController?,
    userName: String? = null,
    userImage: String? = null,
    role: String? = null,
    onSettingsClick: (() -> Unit)? = null,
    onMenuItemClick: ((String) -> Unit)? = null
) {
    val isMenuExpanded by DrawerState.isExpanded
    
    // Only render if menu is expanded to avoid blocking clicks
    if (!isMenuExpanded) {
        return
    }
    
    // Scrim (dark overlay) when drawer is open
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                onClick = { DrawerState.isExpanded.value = false },
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            )
            .zIndex(999f)
    )
    
    // Sidebar drawer
    Box(
        modifier = Modifier.zIndex(1000f)
    ) {
        when (role) {
            "vet" -> VetNavigationDrawerMenu(
                navController = navController,
                onItemClick = { DrawerState.isExpanded.value = false },
                userName = userName,
                userImage = userImage
            )
            "admin" -> AdminNavigationDrawerMenu(
                navController = navController,
                onItemClick = { DrawerState.isExpanded.value = false },
                userName = userName,
                userImage = userImage,
                onSettingsClick = onSettingsClick,
                onMenuItemClick = onMenuItemClick
            )
            "sitter" -> SitterNavigationDrawerMenu(
                navController = navController,
                onItemClick = { DrawerState.isExpanded.value = false },
                userName = userName,
                userImage = userImage
            )
            "trainer" -> TrainerNavigationDrawerMenu(
                navController = navController,
                onItemClick = { DrawerState.isExpanded.value = false },
                userName = userName,
                userImage = userImage
            )
            "salon" -> SalonNavigationDrawerMenu(
                navController = navController,
                onItemClick = { DrawerState.isExpanded.value = false },
                userName = userName,
                userImage = userImage,
                onMenuItemClick = onMenuItemClick
            )
            else -> NavigationDrawerMenu(
                navController = navController,
                onItemClick = { DrawerState.isExpanded.value = false },
                userName = userName,
                userImage = userImage
            )
        }
    }
}

@Composable
fun NavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    val menuItems = listOf(
        DrawerMenuItem("Home", Icons.Filled.Home, "home"),
        DrawerMenuItem("Discover", Icons.Filled.Search, "discover"),
        DrawerMenuItem("My Pets", Icons.Filled.Favorite, "myPets"),
        DrawerMenuItem("Conversations", Icons.Filled.Email, "conversations"),
        DrawerMenuItem("My Bookings", Icons.Filled.DateRange, "booking_list"),
        DrawerMenuItem("Calendar", Icons.Filled.Edit, "calendar"),
        DrawerMenuItem("Community", Icons.Filled.Menu, "community"),
        DrawerMenuItem("Marketplace", Icons.Filled.ShoppingCart, "marketplace_listings"),
        DrawerMenuItem("Adoption", Icons.Filled.FavoriteBorder, "adoption_listings")
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Menu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = VetStroke.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Menu items
            menuItems.forEach { item ->
                val menuItem = item as DrawerMenuItem
                val isSelected = currentRoute == menuItem.route

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate(menuItem.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            onItemClick()
                        }
                        .background(if (isSelected) HeaderBackground else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = menuItem.icon,
                        contentDescription = menuItem.label,
                        tint = if (isSelected) TextPrimary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = menuItem.label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // User Profile Section at Bottom
            if (userName != null) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = VetStroke.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate("profile") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            onItemClick()
                        }
                        .background(if (currentRoute == "profile") HeaderBackground else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (userImage != null && userImage.isNotBlank()) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(userImage),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "View Profile",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = 180f }
                    )
                }
            }
        }
    }
}

// Role-specific Navigation Drawer Menus

@Composable
fun VetNavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    val menuItems = listOf(
        DrawerMenuItem("Dashboard", Icons.Filled.Home, "vet_main"),
        DrawerMenuItem("Patients", Icons.Filled.Person, "vet_main"), // Will show patients tab
        DrawerMenuItem("Appointments", Icons.Filled.DateRange, "vet_main"), // Will show appointments tab
        DrawerMenuItem("Messages", Icons.Filled.Email, "conversations"),
        DrawerMenuItem("Profile", Icons.Filled.Person, "vet_profile")
    )

    DrawerMenuContent(
        menuItems = menuItems,
        navController = navController,
        onItemClick = onItemClick,
        userName = userName,
        userImage = userImage,
        currentRoute = currentRoute,
        title = "Veterinarian",
        onSettingsClick = null,
        onMenuItemClick = null
    )
}

@Composable
fun AdminNavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null,
    onSettingsClick: (() -> Unit)? = null,
    onMenuItemClick: ((String) -> Unit)? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    val menuItems = listOf(
        DrawerMenuItem("Dashboard", Icons.Filled.Home, "admin_main"),
        DrawerMenuItem("Pending Approvals", Icons.Filled.Notifications, "admin_main"), // Will show pending tab
        DrawerMenuItem("All Users", Icons.Filled.Person, "admin_main"), // Will show all users tab
        DrawerMenuItem("Reports", Icons.Filled.Info, "admin_main"),
        DrawerMenuItem("Settings", Icons.Filled.Settings, "admin_main")
    )

    DrawerMenuContent(
        menuItems = menuItems,
        navController = navController,
        onItemClick = onItemClick,
        userName = userName,
        userImage = userImage,
        currentRoute = currentRoute,
        title = "Admin",
        onSettingsClick = onSettingsClick,
        onMenuItemClick = onMenuItemClick
    )
}

@Composable
fun SitterNavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    val menuItems = listOf(
        DrawerMenuItem("Dashboard", Icons.Filled.Home, "sitter_main"),
        DrawerMenuItem("Sitting Requests", Icons.Filled.Notifications, "sitter_main"), // Will show requests tab
        DrawerMenuItem("Active Bookings", Icons.Filled.DateRange, "sitter_main"), // Will show bookings tab
        DrawerMenuItem("Calendar", Icons.Filled.DateRange, "calendar"),
        DrawerMenuItem("Messages", Icons.Filled.Email, "conversations"),
        DrawerMenuItem("Profile", Icons.Filled.Person, "sitter_profile")
    )

    DrawerMenuContent(
        menuItems = menuItems,
        navController = navController,
        onItemClick = onItemClick,
        userName = userName,
        userImage = userImage,
        currentRoute = currentRoute,
        title = "Pet Sitter",
        onSettingsClick = null,
        onMenuItemClick = null
    )
}

@Composable
fun TrainerNavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    val menuItems = listOf(
        DrawerMenuItem("Dashboard", Icons.Filled.Home, "trainer_main"),
        DrawerMenuItem("Training Pets", Icons.Filled.Star, "trainer_main"), // Will show training pets tab
        DrawerMenuItem("Calendar", Icons.Filled.DateRange, "trainer_main"), // Will show calendar tab
        DrawerMenuItem("Messages", Icons.Filled.Email, "conversations"),
        DrawerMenuItem("Profile", Icons.Filled.Person, "trainer_main") // Will navigate to trainer profile
    )

    DrawerMenuContent(
        menuItems = menuItems,
        navController = navController,
        onItemClick = onItemClick,
        userName = userName,
        userImage = userImage,
        currentRoute = currentRoute,
        title = "Trainer",
        onSettingsClick = null,
        onMenuItemClick = null
    )
}

@Composable
fun SalonNavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null,
    onMenuItemClick: ((String) -> Unit)? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    val menuItems = listOf(
        DrawerMenuItem("Dashboard", Icons.Filled.Home, "salon_main"),
        DrawerMenuItem("Appointments", Icons.Filled.DateRange, "salon_main"), // Will show appointments tab
        DrawerMenuItem("Services", Icons.Filled.Favorite, "salon_main"), // Will show services tab
        DrawerMenuItem("Calendar", Icons.Filled.DateRange, "calendar"),
        DrawerMenuItem("Messages", Icons.Filled.Email, "conversations"),
        DrawerMenuItem("Profile", Icons.Filled.Person, "salon_profile")
    )

    DrawerMenuContent(
        menuItems = menuItems,
        navController = navController,
        onItemClick = onItemClick,
        userName = userName,
        userImage = userImage,
        currentRoute = currentRoute,
        title = "Pet Salon",
        onSettingsClick = null,
        onMenuItemClick = onMenuItemClick
    )
}

@Composable
private fun DrawerMenuContent(
    menuItems: List<DrawerMenuItem>,
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String?,
    userImage: String?,
    currentRoute: String?,
    title: String,
    onSettingsClick: (() -> Unit)? = null,
    onMenuItemClick: ((String) -> Unit)? = null
) {
    val items = menuItems
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = VetStroke.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Menu items
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val isSettingsItem = item.label == "Settings" && onSettingsClick != null
                val isAdminMenuItem = title == "Admin" && onMenuItemClick != null && !isSettingsItem
                val isSalonMenuItem = title == "Pet Salon" && onMenuItemClick != null && 
                    (item.route == "salon_main" || item.label == "Dashboard" || item.label == "Appointments" || item.label == "Services")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSettingsItem) {
                                onSettingsClick()
                                onItemClick()
                            } else if (isAdminMenuItem || isSalonMenuItem) {
                                // For admin and salon, use the callback to switch tabs
                                onMenuItemClick(item.label)
                                onItemClick()
                            } else {
                                navController?.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                onItemClick()
                            }
                        }
                        .background(if (isSelected) HeaderBackground else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) TextPrimary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // User Profile Section at Bottom
            if (userName != null) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = VetStroke.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (title) {
                                "Veterinarian" -> {
                                    navController?.navigate("vet_profile") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                "Pet Sitter" -> {
                                    navController?.navigate("sitter_profile") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                "Trainer" -> {
                                    navController?.navigate("trainer_profile") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                "Pet Salon" -> {
                                    navController?.navigate("salon_profile") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                "Admin" -> {
                                    // For admin, navigate to admin profile screen
                                    navController?.navigate("admin_profile") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                else -> {
                                    // For owner/default, navigate to profile
                                    navController?.navigate("profile") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            onItemClick()
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (userImage != null && userImage.isNotBlank()) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(userImage),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "View Profile",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = 180f }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopNavIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                onClick = {
                    Log.d("TopNavIconButton", "Button clicked: $contentDescription")
                    onClick()
                },
                enabled = true
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = VetStroke.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TopNavBadgeButton(
    icon: ImageVector,
    contentDescription: String?,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        TopNavIconButton(icon = icon, contentDescription = contentDescription, onClick = onClick)

        if (badgeCount > 0) {
            Text(
                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Red, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
