package tn.petcare_android.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tn.petcare_android.data.model.chat.Message
import tn.petcare_android.ui.components.TopNavBar
import tn.petcare_android.ui.theme.*
import tn.petcare_android.viewmodel.chat.ChatViewModel

/**
 * ChatViewScreen matching iOS ChatView
 * Individual conversation screen with message list and input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewScreen(
    navController: NavHostController,
    themePreference: tn.petcare_android.data.storage.ThemePreference,
    recipientId: String,
    recipientName: String,
    currentUserId: String,
    conversationId: String? = null,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Log for debugging
    LaunchedEffect(error) {
        error?.let {
            android.util.Log.e("ChatViewScreen", "Error: $it")
        }
    }
    
    LaunchedEffect(messages.size) {
        android.util.Log.d("ChatViewScreen", "Messages count: ${messages.size}")
    }
    
    var messageText by remember { mutableStateOf("") }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var actualConversationId by remember { mutableStateOf(conversationId) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // iOS: onAppear - get or create conversation, then ALWAYS load messages
    LaunchedEffect(conversationId, recipientId) {
        if (conversationId != null) {
            // We have a conversation ID (from conversations list) - load messages directly
            actualConversationId = conversationId
            viewModel.loadMessages(conversationId)
        } else if (actualConversationId == null && recipientId.isNotEmpty()) {
            // Get or create conversation first (like iOS) - new conversation from Discover
            val conversation = viewModel.getOrCreateConversation(recipientId)
            if (conversation != null) {
                actualConversationId = conversation.normalizedId
                // iOS: ALWAYS load messages after getting conversation
                viewModel.loadMessages(conversation.normalizedId)
            }
        } else if (actualConversationId != null) {
            // Existing conversation - just load messages
            viewModel.loadMessages(actualConversationId!!)
        }
    }
    
    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = recipientName,
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading && messages.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VetCanyon
                        )
                    }
                    messages.isEmpty() -> {
                        Text(
                            text = "No messages yet. Start the conversation!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    isFromCurrentUser = message.normalizedSenderId == currentUserId,
                                    onEdit = {
                                        editingMessageId = message.normalizedId
                                        messageText = message.content
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        coroutineScope.launch {
                                            viewModel.deleteMessage(message.normalizedId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Message Input
            MessageInput(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank() && recipientId.isNotEmpty()) {
                        val textToSend = messageText.trim()
                        messageText = "" // Clear immediately for better UX (like iOS)
                        
                        coroutineScope.launch {
                            // Send message with recipientId and optional conversationId
                            // Backend creates conversation if it doesn't exist
                            val success = viewModel.sendMessage(recipientId, textToSend, actualConversationId)
                            if (!success) {
                                // Restore message if send failed (like iOS)
                                messageText = textToSend
                            } else if (actualConversationId == null) {
                                // Update conversation ID after first message
                                val conversations = viewModel.conversations.value
                                val newConv = conversations.firstOrNull { conv ->
                                    conv.participants?.any { it.normalizedId == recipientId } == true
                                }
                                if (newConv != null) {
                                    actualConversationId = newConv.normalizedId
                                }
                            }
                        }
                    }
                },
                enabled = recipientId.isNotEmpty() // Always enable if we have recipient
            )
        }
    }
    
    // Edit Message Dialog
    if (showEditDialog && editingMessageId != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingMessageId = null
                messageText = ""
            },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Edit your message...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VetCanyon,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingMessageId?.let { id ->
                            coroutineScope.launch {
                                viewModel.editMessage(id, messageText.trim())
                                showEditDialog = false
                                editingMessageId = null
                                messageText = ""
                            }
                        }
                    }
                ) {
                    Text("Save", color = VetCanyon)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingMessageId = null
                        messageText = ""
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Format timestamp
    val timestamp = try {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(message.createdAt)
        if (date != null) {
            val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            timeFormat.format(date)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromCurrentUser) {
            // Other person's messages on the left
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Message bubble
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .border(1.dp, VetStroke, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (message.isDeleted == true) {
                            Text(
                                text = "This message has been deleted",
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                text = message.content,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                
                // Timestamp and edited indicator
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (timestamp.isNotEmpty()) {
                        Text(
                            text = timestamp,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (message.isEdited || message.updatedAt != null) {
                        Text(
                            text = "(edited)",
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(60.dp))
        } else {
            // My messages on the right
            Spacer(modifier = Modifier.width(60.dp))
            
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Message bubble
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VetCanyon,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clickable { showMenu = true }
                ) {
                    Box {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (message.isDeleted == true) {
                                Text(
                                    text = "This message has been deleted",
                                    fontSize = 14.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    text = message.content,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        // Menu for current user's messages
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (message.isDeleted != true) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Timestamp and edited indicator
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (timestamp.isNotEmpty()) {
                        Text(
                            text = timestamp,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (message.isEdited || message.updatedAt != null) {
                        Text(
                            text = "(edited)",
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = VetCanyon,
                    unfocusedBorderColor = VetStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                enabled = enabled
            )
            
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(), // Only check if text is not blank
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (enabled && text.isNotBlank()) VetCanyon else VetCanyon.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
