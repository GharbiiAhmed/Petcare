package tn.petcare_android.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.community.Comment
import tn.petcare_android.data.model.community.Post
import tn.petcare_android.data.model.community.ReactionType
import tn.petcare_android.ui.theme.*
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.util.ImageFileHelper
import tn.petcare_android.viewmodel.community.CommunityViewModel
import tn.petcare_android.data.storage.UserManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference
) {
    val context = LocalContext.current
    val viewModel: CommunityViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val userManager = remember { UserManager(context) }
    val currentUserId by userManager.getUserId().collectAsState(initial = null)
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = All Posts, 1 = My Posts
    
    // Load appropriate posts based on selected tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadPosts(refresh = true)
        } else {
            viewModel.loadMyPosts(refresh = true)
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                TopNavBar(
                    title = "Community",
                    navController = navController,
                    showBackButton = false,
                    showMenuButton = true,
                    onSettingsClick = { navController.navigate("profile") },
                    actions = {
                        IconButton(onClick = { 
                            if (selectedTab == 0) {
                                viewModel.loadPosts(refresh = true)
                            } else {
                                viewModel.loadMyPosts(refresh = true)
                            }
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (isRefreshing) VetCanyon else TextPrimary
                            )
                        }
                    }
                )
                
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = VetCanyon,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = VetCanyon
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "All Posts",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "My Posts",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = VetCanyon,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        onReactionClick = { reactionType ->
                            viewModel.reactToPost(post.id, reactionType)
                        },
                        onUserClick = { userId ->
                            // Check if it's the current user
                            if (userId == currentUserId) {
                                navController.navigate("profile")
                            } else {
                                navController.navigate("user_profile/$userId")
                            }
                        },
                        onDeleteClick = { viewModel.deletePost(post.id) },
                        onReportClick = { viewModel.reportPost(post.id) },
                        onAddComment = { text ->
                            viewModel.addComment(post.id, text)
                        },
                        onDeleteComment = { commentId ->
                            viewModel.deleteComment(post.id, commentId)
                        },
                        showDeleteButton = post.userId == currentUserId
                    )
                }
                
                // Load more trigger
                item {
                    LaunchedEffect(Unit) {
                        if (selectedTab == 0) {
                            viewModel.loadMorePosts()
                        } else {
                            viewModel.loadMoreMyPosts()
                        }
                    }
                    
                    if (isLoading && posts.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = VetCanyon,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
            
            // Error message
            error?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(it)
                }
            }
            
            // Initial loading
            if (isLoading && posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VetCanyon)
                }
            }
        }
    }
    
    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = {
                showCreatePostDialog = false
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    currentUserId: String?,
    onReactionClick: (ReactionType) -> Unit,
    onUserClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    onAddComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    showDeleteButton: Boolean = false
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // User header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.userProfileImage ?: "https://via.placeholder.com/150")
                        .crossfade(true)
                        .build(),
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VetCanyon.copy(alpha = 0.2f))
                        .clickable { onUserClick(post.userId) },
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserClick(post.userId) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        post.userRole?.let { role ->
                            when (role) {
                                "veterinarian" -> Text(
                                    text = " • Vet",
                                    fontSize = 14.sp,
                                    color = VetCanyon,
                                    fontWeight = FontWeight.Medium
                                )
                                "pet-sitter" -> Text(
                                    text = " • Sitter",
                                    fontSize = 14.sp,
                                    color = VetCanyon,
                                    fontWeight = FontWeight.Medium
                                )
                                // Don't show badge for "owner" role
                            }
                        }
                    }
                    Text(
                        text = formatTimeAgo(post.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (showDeleteButton) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete post",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Show report button for posts that aren't the current user's
                    if (!showDeleteButton && currentUserId != null) {
                        IconButton(onClick = onReportClick) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Report post",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Caption
            post.caption?.let { caption ->
                if (caption.isNotBlank()) {
                    Text(
                        text = caption,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Pet image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.petImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Pet photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            
            // Reactions summary
            if (post.likes > 0 || post.loves > 0 || post.hahas > 0 || post.angries > 0 || post.cries > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (post.likes > 0) {
                            ReactionCount(emoji = "👍", count = post.likes)
                        }
                        if (post.loves > 0) {
                            ReactionCount(emoji = "❤️", count = post.loves)
                        }
                        if (post.hahas > 0) {
                            ReactionCount(emoji = "😂", count = post.hahas)
                        }
                        if (post.angries > 0) {
                            ReactionCount(emoji = "😠", count = post.angries)
                        }
                        if (post.cries > 0) {
                            ReactionCount(emoji = "😢", count = post.cries)
                        }
                    }
                }
            }
            
            Divider()
            
            // Reaction button with picker
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ReactionButton(
                        currentReaction = post.userReaction,
                        onClick = { showReactionPicker = !showReactionPicker },
                        modifier = Modifier.weight(1f)
                    )
                    
                    TextButton(
                        onClick = { showComments = !showComments },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Comment",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (post.comments.isNotEmpty()) "Comments (${post.comments.size})" else "Comment",
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Reaction picker popup
                if (showReactionPicker) {
                    ReactionPicker(
                        onReactionSelected = { reaction ->
                            onReactionClick(reaction)
                            showReactionPicker = false
                        },
                        onDismiss = { showReactionPicker = false }
                    )
                }
            }
            
            // Comments section
            if (showComments) {
                Divider()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Comments list
                    post.comments.forEach { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            onDeleteClick = { onDeleteComment(comment.id) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    // Add comment input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Write a comment...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onAddComment(commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (commentText.isNotBlank()) VetCanyon else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comment.userProfileImage ?: "https://via.placeholder.com/150")
                    .crossfade(true)
                    .build(),
                contentDescription = "Commenter avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VetCanyon.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = comment.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    comment.userRole?.let { role ->
                        if (role == "vet" || role == "sitter") {
                            Text(
                                text = "• ${if (role == "vet") "Vet" else "Sitter"}",
                                fontSize = 12.sp,
                                color = VetCanyon,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = formatTimeAgo(comment.createdAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        
        if (comment.userId == currentUserId) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete comment",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ReactionCount(emoji: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 14.sp
        )
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ReactionButton(
    currentReaction: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, label, isActive) = when (currentReaction) {
        "like" -> Triple("👍", "Like", true)
        "love" -> Triple("❤️", "Love", true)
        "haha" -> Triple("😂", "Haha", true)
        "angry" -> Triple("😠", "Angry", true)
        "cry" -> Triple("😢", "Cry", true)
        else -> Triple("👍", "Like", false)
    }
    
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isActive) VetCanyon else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ReactionPicker(
    onReactionSelected: (ReactionType) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ReactionType.values().forEach { reaction ->
                IconButton(
                    onClick = { onReactionSelected(reaction) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = reaction.emoji,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CommunityViewModel = viewModel()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }
    
    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Create Post") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image preview or picker button
                if (selectedImageUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    TextButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Image")
                    }
                } else {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VetCanyon
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Pet Image")
                    }
                }
                
                // Caption input
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isUploading
                )
                
                // Upload progress
                if (isUploading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = VetCanyon,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uploadProgress != null) 
                                "Uploading... ${(uploadProgress!! * 100).toInt()}%" 
                            else "Uploading...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedImageUri?.let { uri ->
                        isUploading = true
                        val imageFile = ImageFileHelper.uriToFile(context, uri)
                        if (imageFile != null) {
                            viewModel.createPost(
                                imageFile = imageFile,
                                caption = caption.ifBlank { null }
                            )
                            // Wait a bit for upload to complete
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(1500)
                                isUploading = false
                                onPostCreated()
                            }
                        } else {
                            isUploading = false
                        }
                    }
                },
                enabled = selectedImageUri != null && !isUploading
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTimeAgo(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return timestamp
        
        val now = System.currentTimeMillis()
        val diff = now - date.time
        
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> {
                val displayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                displayFormat.format(date)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}
