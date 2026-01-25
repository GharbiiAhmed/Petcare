package tn.petcare_android.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.petcare_android.ui.theme.*

/**
 * Payment Screen for entering card information
 * Static UI - card details are not actually processed
 * Displayed as a ModalBottomSheet popup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    amount: String = "$30/month",
    onPaymentComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var showCvv by remember { mutableStateOf(false) }
    
    // Format card number (add spaces every 4 digits)
    val formattedCardNumber = remember(cardNumber) {
        cardNumber.filter { it.isDigit() }
            .chunked(4)
            .joinToString(" ")
            .take(19) // Max 16 digits + 3 spaces
    }
    
    // Format expiry date (MM/YY)
    val formattedExpiry = remember(expiryDate) {
        val digits = expiryDate.filter { it.isDigit() }
        when {
            digits.length <= 2 -> digits
            else -> "${digits.take(2)}/${digits.drop(2).take(2)}"
        }
    }
    
    // Validation
    val isCardNumberValid = cardNumber.filter { it.isDigit() }.length == 16
    val isNameValid = cardHolderName.trim().length >= 3
    val isExpiryValid = expiryDate.filter { it.isDigit() }.length == 4
    val isCvvValid = cvv.length == 3 || cvv.length == 4
    
    val canSubmit = isCardNumberValid && isNameValid && isExpiryValid && isCvvValid
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f),
        containerColor = PageBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .padding(vertical = 12.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 4.dp,
                    color = TextSecondary.copy(alpha = 0.3f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Details",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OrangeAccent
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scrollable content
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CARD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 2.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Card Number Display
                        Text(
                            text = formattedCardNumber.ifEmpty { "1234 5678 9012 3456" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "CARDHOLDER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cardHolderName.ifEmpty { "JOHN DOE" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "EXPIRES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedExpiry.ifEmpty { "MM/YY" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Card Number Input
                OutlinedTextField(
                    value = formattedCardNumber,
                    onValueChange = { newValue ->
                        cardNumber = newValue.filter { it.isDigit() }.take(16)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Card Number") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    leadingIcon = {
                        Icon(Icons.Default.Info, "Card", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (cardNumber.isNotEmpty()) {
                            Icon(
                                imageVector = if (isCardNumberValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isCardNumberValid) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = if (cardNumber.isNotEmpty() && !isCardNumberValid) Color(0xFFF59E0B) else VetCanyon,
                        unfocusedBorderColor = VetStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                // Card Holder Name
                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = { cardHolderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cardholder Name") },
                    placeholder = { Text("John Doe") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, "Name", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (cardHolderName.isNotEmpty()) {
                            Icon(
                                imageVector = if (isNameValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isNameValid) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = if (cardHolderName.isNotEmpty() && !isNameValid) Color(0xFFF59E0B) else VetCanyon,
                        unfocusedBorderColor = VetStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                // Expiry and CVV Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Expiry Date
                    OutlinedTextField(
                        value = formattedExpiry,
                        onValueChange = { newValue ->
                            expiryDate = newValue.filter { it.isDigit() }.take(4)
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Expiry") },
                        placeholder = { Text("MM/YY") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, "Expiry", tint = TextSecondary)
                        },
                        trailingIcon = {
                            if (expiryDate.isNotEmpty()) {
                                Icon(
                                    imageVector = if (isExpiryValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isExpiryValid) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = if (expiryDate.isNotEmpty() && !isExpiryValid) Color(0xFFF59E0B) else VetCanyon,
                            unfocusedBorderColor = VetStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    
                    // CVV
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { newValue ->
                            cvv = newValue.filter { it.isDigit() }.take(4)
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "CVV", tint = TextSecondary)
                        },
                        trailingIcon = {
                            if (cvv.isNotEmpty()) {
                                Row {
                                    if (isCvvValid) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { showCvv = !showCvv }) {
                                        Icon(
                                            imageVector = if (showCvv) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = if (showCvv) "Hide CVV" else "Show CVV",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        },
                        visualTransformation = if (showCvv) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = if (cvv.isNotEmpty() && !isCvvValid) Color(0xFFF59E0B) else VetCanyon,
                            unfocusedBorderColor = VetStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Security Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Security",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Your payment information is secure and encrypted",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Extra spacing at bottom for scroll
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pay Button (Fixed at bottom)
            Button(
                onClick = {
                    if (canSubmit) {
                        onPaymentComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSubmit) OrangeAccent else OrangeAccent.copy(alpha = 0.4f)
                ),
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, "Pay", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pay $amount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
